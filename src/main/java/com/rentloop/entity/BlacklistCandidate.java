package com.rentloop.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "blacklist_candidates")
@Data
@NoArgsConstructor
public class BlacklistCandidate {

    @Id
    private String id;

    private String userId;

    private String userName;

    private String userEmail;

    private String userRole;

    private Double avgRating;

    private Integer totalReviews;

    private Integer consecutiveLowRatings;

    private String aiReason;

    private CandidateStatus status = CandidateStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    public enum CandidateStatus { PENDING, SUSPENDED, REJECTED }
}
