package com.rentloop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rating_analysis_logs", indexes = {
    @Index(name = "idx_analysis_log_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
public class RatingAnalysisLog {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "consecutive_low_ratings")
    private Integer consecutiveLowRatings;

    @Column(name = "sentiment_score")
    private Double sentimentScore;

    @Column(name = "negative_keywords", columnDefinition = "TEXT")
    private String negativeKeywords;

    @Column(name = "flagged")
    private Boolean flagged = false;

    @Column(name = "flag_reason", columnDefinition = "TEXT")
    private String flagReason;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt = LocalDateTime.now();
}
