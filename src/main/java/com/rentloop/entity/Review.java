package com.rentloop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {
    @Id
    private String id;

    @Column(name = "rental_id", nullable = false)
    private String rentalId;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "renter_id", nullable = false)
    private String renterId;

    @Column(name = "reviewer_id", nullable = false)
    private String reviewerId;

    private Integer rating;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    @Column(name = "review_date")
    private LocalDate reviewDate = LocalDate.now();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
