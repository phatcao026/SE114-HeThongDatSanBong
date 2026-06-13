package com.example.backend.repository;

import com.example.backend.entity.FieldReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldReviewRepository extends JpaRepository<FieldReview, Long> {

    List<FieldReview> findByFieldIdOrderByCreatedAtDesc(Long fieldId);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT COALESCE(AVG(fr.rating), 0.0) FROM FieldReview fr WHERE fr.fieldId = :fieldId")
    Double getAverageRatingForField(@Param("fieldId") Long fieldId);

    @Query("SELECT COUNT(fr) FROM FieldReview fr WHERE fr.fieldId = :fieldId")
    Long getReviewCountForField(@Param("fieldId") Long fieldId);
}
