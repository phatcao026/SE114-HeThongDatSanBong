package com.example.timsanbong.ui.owner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timsanbong.data.model.Booking;
import com.example.timsanbong.data.repository.OwnerBookingRepository;
import com.example.timsanbong.utils.RepositoryCallback;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OwnerBookingViewModel extends AndroidViewModel {
    private static final String TAG = "OwnerBookingViewModel";
    private final OwnerBookingRepository repository = new OwnerBookingRepository();
    private final MutableLiveData<List<Booking>> allBookings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<Booking>> filteredBookings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Long> busyBookingId = new MutableLiveData<>(-1L);
    private final MutableLiveData<BookingSummary> bookingSummary = new MutableLiveData<>(new BookingSummary());

    // Filters
    private String currentSelectedDate = null;
    private String currentSelectedStatus = "ALL";
    private String currentQuery = "";

    private final ExecutorService executorService;

    public OwnerBookingViewModel(@NonNull Application application) {
        super(application);
        executorService = Executors.newSingleThreadExecutor();
        // Observe allBookings to trigger filtering and summary calculation
        allBookings.observeForever(bookings -> {
            applyFiltersAndCalculateSummary();
        });
    }

    // LiveData for UI to observe
    public LiveData<List<Booking>> getFilteredBookings() {
        return filteredBookings;
    }

    public LiveData<BookingSummary> getBookingSummary() {
        return bookingSummary;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Long> getBusyBookingId() {
        return busyBookingId;
    }

    // --- Filter setters ---
    public void setSelectedDate(String date) {
        if (!safeString(currentSelectedDate).equals(safeString(date))) {
            currentSelectedDate = date;
            applyFiltersAndCalculateSummary();
        }
    }

    public void setSelectedStatus(String status) {
        if (!safeString(currentSelectedStatus).equals(safeString(status))) {
            currentSelectedStatus = status;
            applyFiltersAndCalculateSummary();
        }
    }

    public void setSearchQuery(String query) {
        if (!safeString(currentQuery).equals(safeString(query))) {
            currentQuery = query;
            applyFiltersAndCalculateSummary();
        }
    }

    public void clearFilters() {
        currentSelectedDate = null;
        currentSelectedStatus = "ALL";
        currentQuery = "";
        applyFiltersAndCalculateSummary();
    }
    // --- End Filter setters ---

    public void loadBookings() {
        loading.setValue(true);
        repository.getOwnerBookings(getApplication(), new RepositoryCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> data) {
                loading.postValue(false);
                allBookings.postValue(data == null ? Collections.emptyList() : data);
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                message.postValue(error);
                allBookings.postValue(Collections.emptyList()); // Clear data on error
            }
        });
    }

    public void checkInBooking(long bookingId) {
        mutateBooking(bookingId, "Check-in thành công.", callback ->
                repository.checkInBooking(getApplication(), bookingId, callback)
        );
    }

    public void checkOutBooking(long bookingId, String paymentMethod) {
        mutateBooking(bookingId, "Thu nốt tiền thành công.", callback ->
                repository.checkOutBooking(getApplication(), bookingId, paymentMethod, callback)
        );
    }

    public void markNoShow(long bookingId) {
        mutateBooking(bookingId, "Đã đánh dấu no-show.", callback ->
                repository.markNoShow(getApplication(), bookingId, callback)
        );
    }

    // These methods are not directly called by UI based on current fragments, but kept for completeness
    public void confirmBooking(long bookingId) {
        mutateBooking(bookingId, "Xác nhận booking thành công.", callback ->
                repository.confirmBooking(getApplication(), bookingId, callback)
        );
    }

    public void completeBooking(long bookingId) {
        mutateBooking(bookingId, "Hoàn tất booking thành công.", callback ->
                repository.completeBooking(getApplication(), bookingId, callback)
        );
    }

    public void cancelBooking(long bookingId) {
        mutateBooking(bookingId, "Hủy booking thành công.", callback ->
                repository.cancelBooking(getApplication(), bookingId, callback)
        );
    }

    private void mutateBooking(long bookingId, String successMessage, RepositoryMutation mutation) {
        if (bookingId <= 0) {
            message.postValue("Booking không hợp lệ.");
            return;
        }
        busyBookingId.postValue(bookingId);
        loading.postValue(true);
        mutation.run(new RepositoryCallback<Booking>() {
            @Override
            public void onSuccess(Booking data) {
                loading.postValue(false);
                busyBookingId.postValue(-1L);
                message.postValue(data != null && data.getStatus() != null
                        ? successMessage + " Trạng thái mới: " + data.getStatus()
                        : successMessage);
                loadBookings(); // Reload all bookings to ensure fresh data
            }

            @Override
            public void onError(String error) {
                loading.postValue(false);
                busyBookingId.postValue(-1L);
                message.postValue(error);
            }
        });
    }

    private void applyFiltersAndCalculateSummary() {
        executorService.submit(() -> {
            List<Booking> currentBookings = allBookings.getValue();
            if (currentBookings == null) {
                currentBookings = Collections.emptyList();
            }

            List<Booking> newFilteredBookings = new ArrayList<>();
            int pendingCount = 0;
            BigDecimal depositSum = BigDecimal.ZERO;

            String queryLower = safeString(currentQuery).toLowerCase(Locale.US);
            String selectedStatusUpper = safeString(currentSelectedStatus).toUpperCase(Locale.US);
            String selectedDate = safeString(currentSelectedDate);


            for (Booking booking : currentBookings) {
                String bookingStatus = safeStatus(booking);
                String bookingDate = safeString(booking.getBookingDate());

                // Filter by status
                if (!"ALL".equals(selectedStatusUpper) && !selectedStatusUpper.equals(bookingStatus)) {
                    continue;
                }

                // Filter by date
                if (!selectedDate.isEmpty() && !selectedDate.equals(bookingDate)) {
                    continue;
                }

                // Filter by search query
                if (!queryLower.isEmpty()) {
                    String searchSource = buildSearchSource(booking).toLowerCase(Locale.US);
                    if (!searchSource.contains(queryLower)) {
                        continue;
                    }
                }

                newFilteredBookings.add(booking);

                // Calculate summary stats for all bookings (not just filtered)
                // Note: The original implementation calculated depositSum for "DEPOSIT_PAID", "CONFIRMED", "COMPLETED"
                // and pendingCount for "PENDING" based on `allBookings`.
                // For simplicity, I'll calculate summary based on `allBookings` first,
                // and then apply filters to get `filteredBookings`.
                // To keep the original summary logic, summary must be calculated on `allBookings` directly.
            }

            // Re-calculate summary on all bookings after fetching, or only on filtered if preferred.
            // Sticking to original fragment's logic: summary based on all bookings.
            int totalBookings = currentBookings.size();
            int currentPendingCount = 0;
            BigDecimal currentDepositSum = BigDecimal.ZERO;
            for (Booking booking : currentBookings) {
                String status = safeStatus(booking);
                if ("PENDING".equals(status)) {
                    currentPendingCount++;
                }
                if ("DEPOSIT_PAID".equals(status) || "CONFIRMED".equals(status) || "COMPLETED".equals(status)) {
                    currentDepositSum = currentDepositSum.add(BigDecimal.valueOf(booking.getDepositAmount()));
                }
            }

            // Sort filtered bookings by date and then ID (desc)
            Collections.sort(newFilteredBookings, (a, b) -> {
                int dateCompare = safeString(a.getBookingDate()).compareTo(safeString(b.getBookingDate()));
                if (dateCompare != 0) {
                    return dateCompare;
                }
                return Long.compare(b.getId(), a.getId());
            });

            filteredBookings.postValue(newFilteredBookings);
            bookingSummary.postValue(new BookingSummary(totalBookings, currentDepositSum, currentPendingCount));
            Log.d(TAG, "Bookings filtered and summary calculated. Filtered size: " + newFilteredBookings.size());
        });
    }

    private String buildSearchSource(Booking booking) {
        StringBuilder builder = new StringBuilder();
        builder.append("#").append(booking.getId()).append(' ');
        builder.append(safeString(booking.getFieldName())).append(' ');
        builder.append(safeString(booking.getNote())).append(' ');
        builder.append(safeString(booking.getBookingDate())).append(' ');
        builder.append(safeString(booking.getStartTime())).append(' ');
        builder.append(safeString(booking.getEndTime())).append(' ');
        builder.append(safeStatus(booking)).append(' ');
        builder.append(safeString(String.valueOf(booking.getFieldId())));
        return builder.toString();
    }

    private String safeStatus(Booking booking) {
        return booking.getStatus() == null ? "" : booking.getStatus().toUpperCase(Locale.US);
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        allBookings.removeObserver(bookings -> {}); // Remove observer to prevent memory leak
    }

    private interface RepositoryMutation {
        void run(RepositoryCallback<Booking> callback);
    }

    public static class BookingSummary {
        public int totalBookings;
        public BigDecimal depositCollected;
        public int pendingCount;

        public BookingSummary() {
            this(0, BigDecimal.ZERO, 0);
        }

        public BookingSummary(int totalBookings, BigDecimal depositCollected, int pendingCount) {
            this.totalBookings = totalBookings;
            this.depositCollected = depositCollected;
            this.pendingCount = pendingCount;
        }

        public String formatDepositCollected() {
            return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(depositCollected);
        }

        public String formatPendingBadge() {
            return pendingCount > 0 ? pendingCount + " chờ cọc" : "0 chờ cọc";
        }
    }
}
