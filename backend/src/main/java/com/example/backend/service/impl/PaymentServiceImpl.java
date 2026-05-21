package com.example.backend.service.impl;

import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Field;
import com.example.backend.entity.Payment;
import com.example.backend.exception.AppException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.FieldRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.service.PaymentService;
import com.example.backend.utils.Enums;
import com.example.backend.utils.TokenUtils;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal DEPOSIT_RATE = BigDecimal.valueOf(0.3);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    @Value("${stripe.success.url:http://localhost:3000/payment/success}")
    private String stripeSuccessUrl;

    @Value("${stripe.cancel.url:http://localhost:3000/payment/cancel}")
    private String stripeCancelUrl;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository,
                              FieldRepository fieldRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.fieldRepository = fieldRepository;
    }

    @PostConstruct
    public void initStripe() {
        if (StringUtils.hasText(stripeApiKey)) {
            Stripe.apiKey = stripeApiKey;
        }
    }

    @Override
    @Transactional
    public PaymentResponse createCheckoutSession(Long bookingId) {
        requireStripeApiKey();

        Booking booking = findBooking(bookingId);
        ensureCanPayBooking(booking);
        validateBookingCanBePaid(booking);

        BigDecimal depositAmount = resolveDepositAmount(booking);
        Session session = createStripeCheckoutSession(booking, depositAmount);

        PaymentResponse response = new PaymentResponse();
        response.setUrl(session.getUrl());
        response.setMessage("Stripe checkout session created");
        response.setBookingId(booking.getId());
        response.setUserId(booking.getUserId());
        response.setAmount(depositAmount);
        response.setPaymentMethod(Enums.PaymentMethod.STRIPE);
        response.setStripePaymentIntentId(session.getId());
        response.setStatus(Enums.PaymentStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    @Override
    public List<PaymentResponse> getMyPayments() {
        Long currentUserId = TokenUtils.getCurrentUserId();
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(payment -> toResponse(payment, null, null))
                .toList();
    }

    @Override
    public List<PaymentResponse> getBookingPayments(Long bookingId) {
        Booking booking = findBooking(bookingId);
        ensureCanViewBooking(booking);

        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
                .stream()
                .map(payment -> toResponse(payment, null, null))
                .toList();
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        requireWebhookSecret();

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException ex) {
            throw new AppException(400, "Invalid Stripe webhook signature");
        }

        if (!"checkout.session.completed".equals(event.getType())) {
            return;
        }

        Session session = extractCheckoutSession(event);
        completeCheckoutSession(session);
    }

    private Session createStripeCheckoutSession(Booking booking, BigDecimal depositAmount) {
        try {
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Dat coc san bong")
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("vnd")
                            .setUnitAmount(toStripeAmount(depositAmount))
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(priceData)
                            .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(buildSuccessUrl())
                    .setCancelUrl(stripeCancelUrl)
                    .setClientReferenceId(String.valueOf(booking.getId()))
                    .putMetadata("booking_id", String.valueOf(booking.getId()))
                    .addLineItem(lineItem)
                    .build();

            return Session.create(params);
        } catch (StripeException ex) {
            throw new AppException(400, "Cannot create Stripe checkout session: " + ex.getMessage());
        }
    }

    private void completeCheckoutSession(Session session) {
        Long bookingId = parseBookingId(session);
        Booking booking = findBooking(bookingId);

        if (StringUtils.hasText(session.getPaymentIntent())
                && paymentRepository.findByStripePaymentIntentId(session.getPaymentIntent()).isPresent()) {
            return;
        }

        if (paymentRepository.existsByBookingIdAndStatus(booking.getId(), Enums.PaymentStatus.SUCCESS)) {
            return;
        }

        BigDecimal paidAmount = resolvePaidAmount(session, booking);

        Payment payment = new Payment();
        payment.setBookingId(booking.getId());
        payment.setUserId(booking.getUserId());
        payment.setAmount(paidAmount);
        payment.setPaymentMethod(Enums.PaymentMethod.STRIPE);
        payment.setStripePaymentIntentId(session.getPaymentIntent());
        payment.setStatus(Enums.PaymentStatus.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (booking.getStatus() == Enums.BookingStatus.PENDING) {
            booking.setStatus(Enums.BookingStatus.DEPOSIT_PAID);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(booking);
        }
    }

    private Session extractCheckoutSession(Event event) {
        var stripeObject = event.getDataObjectDeserializer().getObject();
        if (stripeObject.isPresent()) {
            return (Session) stripeObject.get();
        }

        try {
            return (Session) event.getDataObjectDeserializer().deserializeUnsafe();
        } catch (EventDataObjectDeserializationException ex) {
            throw new AppException(400, "Cannot deserialize Stripe checkout session");
        }
    }

    private Long parseBookingId(Session session) {
        String value = session.getClientReferenceId();

        if (!StringUtils.hasText(value) && session.getMetadata() != null) {
            value = session.getMetadata().get("booking_id");
        }

        if (!StringUtils.hasText(value)) {
            throw new AppException(400, "Stripe session does not include booking id");
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new AppException(400, "Invalid booking id in Stripe session");
        }
    }

    private BigDecimal resolvePaidAmount(Session session, Booking booking) {
        if (session.getAmountTotal() != null) {
            return BigDecimal.valueOf(session.getAmountTotal());
        }

        return resolveDepositAmount(booking);
    }

    private void validateBookingCanBePaid(Booking booking) {
        if (booking.getStatus() == Enums.BookingStatus.CANCELLED) {
            throw new AppException(400, "Cancelled booking cannot be paid");
        }
        if (booking.getStatus() == Enums.BookingStatus.DEPOSIT_PAID
                || booking.getStatus() == Enums.BookingStatus.CONFIRMED
                || booking.getStatus() == Enums.BookingStatus.COMPLETED) {
            throw new AppException(400, "Booking deposit has already been paid");
        }
        if (paymentRepository.existsByBookingIdAndStatus(booking.getId(), Enums.PaymentStatus.SUCCESS)) {
            throw new AppException(400, "Booking already has a successful payment");
        }
    }

    private BigDecimal resolveDepositAmount(Booking booking) {
        if (booking.getDepositAmount() != null && booking.getDepositAmount().signum() > 0) {
            return booking.getDepositAmount();
        }

        if (booking.getTotalAmount() == null || booking.getTotalAmount().signum() <= 0) {
            throw new AppException(400, "Booking amount is invalid");
        }

        BigDecimal depositAmount = booking.getTotalAmount().multiply(DEPOSIT_RATE).setScale(2, RoundingMode.HALF_UP);
        booking.setDepositAmount(depositAmount);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        return depositAmount;
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Booking not found"));
    }

    private Field findField(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Field not found"));
    }

    private void ensureCanPayBooking(Booking booking) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        if (booking.getUserId().equals(currentUserId) || TokenUtils.hasRole("ADMIN")) {
            return;
        }

        throw new AppException(403, "Access denied");
    }

    private void ensureCanViewBooking(Booking booking) {
        Long currentUserId = TokenUtils.getCurrentUserId();
        if (booking.getUserId().equals(currentUserId) || TokenUtils.hasRole("ADMIN")) {
            return;
        }

        Field field = findField(booking.getFieldId());
        if (field.getOwnerId() != null && field.getOwnerId().equals(currentUserId)) {
            return;
        }

        throw new AppException(403, "Access denied");
    }

    private PaymentResponse toResponse(Payment payment, String message, String url) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBookingId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStripePaymentIntentId(payment.getStripePaymentIntentId());
        response.setStatus(payment.getStatus());
        response.setCreatedAt(payment.getCreatedAt());
        response.setMessage(message);
        response.setUrl(url);
        return response;
    }

    private long toStripeAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private String buildSuccessUrl() {
        if (stripeSuccessUrl.contains("{CHECKOUT_SESSION_ID}")) {
            return stripeSuccessUrl;
        }

        String separator = stripeSuccessUrl.contains("?") ? "&" : "?";
        return stripeSuccessUrl + separator + "session_id={CHECKOUT_SESSION_ID}";
    }

    private void requireStripeApiKey() {
        if (!StringUtils.hasText(stripeApiKey)) {
            throw new AppException(500, "Stripe secret key is not configured");
        }
        Stripe.apiKey = stripeApiKey;
    }

    private void requireWebhookSecret() {
        if (!StringUtils.hasText(stripeWebhookSecret)) {
            throw new AppException(500, "Stripe webhook secret is not configured");
        }
    }
}
