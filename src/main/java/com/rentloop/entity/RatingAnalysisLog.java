package com.rentloop.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "rating_analysis_logs")
@Data
@NoArgsConstructor
public class RatingAnalysisLog {

    @Id
    private String id;

    private String userId;

    private Double avgRating;

    private Integer totalReviews;

    private Integer consecutiveLowRatings;

    private Double sentimentScore;

    private String negativeKeywords;

    private Boolean flagged = false;

    private String flagReason;

    private LocalDateTime analyzedAt = LocalDateTime.now();
}
