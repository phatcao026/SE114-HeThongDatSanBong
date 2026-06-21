package com.example.backend.controller;

import com.example.backend.dto.request.FieldCreateRequest;
import com.example.backend.dto.request.FieldUpdateRequest;
import com.example.backend.dto.request.TimeSlotCreateRequest;
import com.example.backend.dto.request.TimeSlotUpdateRequest;
import com.example.backend.dto.response.FieldDetailResponse;
import com.example.backend.dto.response.FieldResponse;
import com.example.backend.dto.response.TimeSlotAvailabilityResponse;
import com.example.backend.dto.response.TimeSlotResponse;
import com.example.backend.service.FieldService;
import com.example.backend.utils.Enums;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fields")
public class FieldController {
    private final FieldService fieldService;

    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<FieldResponse>> getOwnerFields() {
        return ResponseEntity.ok(fieldService.getOwnerFields());
    }

    @GetMapping
    public ResponseEntity<List<FieldResponse>> getFields(
            @RequestParam(required = false) Enums.FieldType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(fieldService.getFields(type, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FieldDetailResponse> getFieldById(@PathVariable Long id) {
        return ResponseEntity.ok(fieldService.getFieldById(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<List<TimeSlotAvailabilityResponse>> getFieldAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(fieldService.getFieldAvailability(id, date));
    }

    @PostMapping
    public ResponseEntity<FieldResponse> createField(@Valid @RequestBody FieldCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fieldService.createField(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FieldResponse> updateField(@PathVariable Long id,
                                                     @RequestBody FieldUpdateRequest request) {
        return ResponseEntity.ok(fieldService.updateField(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FieldResponse> deleteField(@PathVariable Long id) {
        return ResponseEntity.ok(fieldService.deleteField(id));
    }

    @PostMapping("/{id}/time-slots")
    public ResponseEntity<TimeSlotResponse> createTimeSlot(@PathVariable Long id,
                                                           @Valid @RequestBody TimeSlotCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fieldService.createTimeSlot(id, request));
    }

    @PutMapping("/{id}/time-slots/{slotId}")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(@PathVariable Long id,
                                                           @PathVariable Long slotId,
                                                           @RequestBody TimeSlotUpdateRequest request) {
        return ResponseEntity.ok(fieldService.updateTimeSlot(id, slotId, request));
    }

    @DeleteMapping("/{id}/time-slots/{slotId}")
    public ResponseEntity<TimeSlotResponse> deleteTimeSlot(@PathVariable Long id,
                                                           @PathVariable Long slotId) {
        return ResponseEntity.ok(fieldService.deleteTimeSlot(id, slotId));
    }
}
