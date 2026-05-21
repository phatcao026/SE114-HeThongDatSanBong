package com.example.backend.repository;

import com.example.backend.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByFieldIdOrderByStartTimeAsc(Long fieldId);

    Optional<TimeSlot> findByFieldIdAndId(Long fieldId, Long id);

    void deleteByFieldId(Long fieldId);
}
