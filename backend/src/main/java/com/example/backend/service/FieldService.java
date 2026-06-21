package com.example.backend.service;

import com.example.backend.dto.request.FieldCreateRequest;
import com.example.backend.dto.request.FieldUpdateRequest;
import com.example.backend.dto.request.TimeSlotCreateRequest;
import com.example.backend.dto.request.TimeSlotUpdateRequest;
import com.example.backend.dto.response.FieldDetailResponse;
import com.example.backend.dto.response.FieldResponse;
import com.example.backend.dto.response.TimeSlotAvailabilityResponse;
import com.example.backend.dto.response.TimeSlotResponse;
import com.example.backend.utils.Enums;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FieldService {
    List<FieldResponse> getFields(Enums.FieldType type, BigDecimal minPrice, BigDecimal maxPrice);

    List<FieldResponse> getOwnerFields();

    FieldDetailResponse getFieldById(Long id);

    List<TimeSlotAvailabilityResponse> getFieldAvailability(Long id, LocalDate date);

    FieldResponse createField(FieldCreateRequest request);

    FieldResponse updateField(Long id, FieldUpdateRequest request);

    FieldResponse deleteField(Long id);

    TimeSlotResponse createTimeSlot(Long fieldId, TimeSlotCreateRequest request);

    TimeSlotResponse updateTimeSlot(Long fieldId, Long slotId, TimeSlotUpdateRequest request);

    TimeSlotResponse deleteTimeSlot(Long fieldId, Long slotId);
}
