package com.example.backend.entity;

import com.example.backend.utils.Enums;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "time_slots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Enums.TimeSlotStatus status;

    @ManyToOne
    @JoinColumn(name = "field_id", insertable = false, updatable = false)
    private Field field;

    @OneToMany(mappedBy = "timeSlot")
    private List<Booking> bookings;

    // getters/setters
    public TimeSlot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public Long getFieldId() { return fieldId; }
    public void setFieldId(Long fieldId) { this.fieldId = fieldId; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Enums.TimeSlotStatus getStatus() { return status; }
    public void setStatus(Enums.TimeSlotStatus status) { this.status = status; }
}
