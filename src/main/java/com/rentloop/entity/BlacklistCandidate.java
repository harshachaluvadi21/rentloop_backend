package com.rentloop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklist_candidates", indexes = {
    @Index(name = "idx_blacklist_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
public class BlacklistCandidate {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "consecutive_low_ratings")
    private Integer consecutiveLowRatings;

    @Column(name = "ai_reason", columnDefinition = "TEXT")
    private String aiReason;

    @Enumerated(EnumType.STRING)
    private CandidateStatus status = CandidateStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum CandidateStatus { PENDING, SUSPENDED, REJECTED }
}
