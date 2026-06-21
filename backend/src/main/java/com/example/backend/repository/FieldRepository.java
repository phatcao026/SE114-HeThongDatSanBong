package com.example.backend.repository;

import com.example.backend.entity.Field;
import com.example.backend.utils.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {

    @EntityGraph(attributePaths = {"timeSlots"})
    List<Field> findAll();

    List<Field> findByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = {"timeSlots"})
    List<Field> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Field> findAllByOrderByCreatedAtDesc();

    List<Field> findByStatusOrderByCreatedAtDesc(Enums.FieldStatus status);

    long countByStatus(Enums.FieldStatus status);

    @EntityGraph(attributePaths = {"timeSlots"})
    @Query("SELECT f FROM Field f WHERE f.id = :id")
    Optional<Field> findByIdWithTimeSlots(@Param("id") Long id);

    // Fixed: use LEFT JOIN FETCH so fields without any timeslot are still included.
    // Price filters are applied only when at least one timeslot matches.
    @Query("SELECT DISTINCT f FROM Field f LEFT JOIN FETCH f.timeSlots ts WHERE " +
            "(:type IS NULL OR f.type = :type) AND " +
            "(:minPrice IS NULL OR ts.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR ts.price <= :maxPrice)")
    List<Field> findFieldsWithFilters(@Param("type") Enums.FieldType type,
                                      @Param("minPrice") BigDecimal minPrice,
                                      @Param("maxPrice") BigDecimal maxPrice);

    // Used for the public browseable list (all fields with timeslots eagerly loaded)
    @EntityGraph(attributePaths = {"timeSlots"})
    @Query("SELECT f FROM Field f ORDER BY f.createdAt DESC")
    List<Field> findAllWithTimeSlots();
}
