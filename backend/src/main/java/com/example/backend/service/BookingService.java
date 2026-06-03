package com.example.backend.service;

import com.example.backend.dto.request.BookingCreateRequest;
import com.example.backend.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    List<BookingResponse> getMyBookings();

    List<BookingResponse> getOwnerBookings();

    BookingResponse getBookingById(Long id);

    BookingResponse createBooking(BookingCreateRequest request);

    BookingResponse cancelBooking(Long id);

    BookingResponse confirmBooking(Long id);

    BookingResponse completeBooking(Long id);
}
