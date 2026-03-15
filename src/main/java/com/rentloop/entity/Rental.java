package com.rentloop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Data
@NoArgsConstructor
public class Rental {
    @Id
    private String id;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "renter_id", nullable = false)
    private String renterId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private Integer days;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private Status status = Status.pending;

    @Column(name = "picked_up")
    private Boolean pickedUp = false;

    private Boolean returned = false;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { pending, approved, rejected, completed, cancelled }
}
