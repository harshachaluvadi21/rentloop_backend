package com.rentloop.service;

import com.rentloop.entity.*;
import com.rentloop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core AI Rating Analysis Service.
 * Monitors rating patterns for all users and flags suspicious activity.
 *
 * Rules:
 * 1. Average rating < 2.5 over at least 3 reviews
 * 2. 3+ consecutive ratings of <= 2
 * 3. Negative sentiment detected in feedback text
 */
@Service
public class RatingAnalysisService {

    private static final double AVG_RATING_THRESHOLD = 2.5;
    private static final int MIN_REVIEWS_FOR_AVG_CHECK = 3;
    private static final int CONSECUTIVE_LOW_THRESHOLD = 3;
    private static final int LOW_RATING_VALUE = 2;

    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;
    private final BlacklistCandidateRepository blacklistRepo;
    private final RatingAnalysisLogRepository analysisLogRepo;
    private final SentimentAnalysisService sentimentService;

    public RatingAnalysisService(ReviewRepository reviewRepo, UserRepository userRepo,
                                  BlacklistCandidateRepository blacklistRepo,
                                  RatingAnalysisLogRepository analysisLogRepo,
                                  SentimentAnalysisService sentimentService) {
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
        this.blacklistRepo = blacklistRepo;
        this.analysisLogRepo = analysisLogRepo;
        this.sentimentService = sentimentService;
    }

    /**
     * Analyze all users. Called by the scheduler or manually.
     * Uses lastAnalyzedAt timestamp to skip users with no new reviews.
     */
    @Transactional
    public void analyzeAllUsers() {
        List<User> users = userRepo.findAll();
        for (User user : users) {
            if (user.getRole() != User.Role.admin) {
                analyzeUser(user);
            }
        }
    }

    /**
     * Analyze a single user's rating history and flag if necessary.
     */
    @Transactional
    public void analyzeUser(User user) {
        String userId = user.getId();

        // Fetch all reviews where this user was reviewed (as owner or renter)
        List<Review> reviewedAsOwner = reviewRepo.findByOwnerId(userId);
        List<Review> reviewedAsRenter = reviewRepo.findByRenterId(userId);

        // Combine all reviews where this user is the SUBJECT (not the reviewer)
        List<Review> subjectReviews = new ArrayList<>();
        subjectReviews.addAll(reviewedAsOwner.stream()
            .filter(r -> !r.getReviewerId().equals(userId)).collect(Collectors.toList()));
        subjectReviews.addAll(reviewedAsRenter.stream()
            .filter(r -> !r.getReviewerId().equals(userId)).collect(Collectors.toList()));

        if (subjectReviews.isEmpty()) return;

        // --- Scheduler Safety: Skip if no new reviews since last analysis ---
        Optional<LocalDateTime> lastAnalyzed = analysisLogRepo.findLastAnalyzedAt(userId);
        if (lastAnalyzed.isPresent()) {
            boolean hasNewReviews = subjectReviews.stream()
                .anyMatch(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(lastAnalyzed.get()));
            if (!hasNewReviews) return; // nothing new, skip
        }

        // Sort by date ascending
        subjectReviews.sort(Comparator.comparing(r ->
            r.getCreatedAt() != null ? r.getCreatedAt() : LocalDateTime.MIN));

        // --- Rule 1: Average Rating Check ---
        double avgRating = subjectReviews.stream()
            .mapToInt(Review::getRating).average().orElse(5.0);
        boolean avgLow = subjectReviews.size() >= MIN_REVIEWS_FOR_AVG_CHECK
            && avgRating < AVG_RATING_THRESHOLD;

        // --- Rule 2: Consecutive Low Ratings ---
        int maxConsecutiveLow = calculateMaxConsecutiveLow(subjectReviews);
        boolean consecutiveLow = maxConsecutiveLow >= CONSECUTIVE_LOW_THRESHOLD;

        // --- Rule 3: Sentiment Analysis on Feedback ---
        List<String> allNegativeKeywords = new ArrayList<>();
        double totalSentimentScore = 0;
        for (Review r : subjectReviews) {
            SentimentAnalysisService.SentimentResult result = sentimentService.analyze(r.getComment());
            allNegativeKeywords.addAll(result.detectedKeywords);
            totalSentimentScore += result.score;
        }
        double avgSentimentScore = totalSentimentScore / subjectReviews.size();
        boolean negativeSentiment = avgSentimentScore < 0.4 || !allNegativeKeywords.isEmpty();

        Set<String> uniqueNegativeKeywords = new LinkedHashSet<>(allNegativeKeywords);

        // --- Build AI Reason ---
        List<String> reasons = new ArrayList<>();
        if (avgLow) reasons.add(String.format("Avg rating %.1f (below %.1f threshold)", avgRating, AVG_RATING_THRESHOLD));
        if (consecutiveLow) reasons.add(String.format("%d consecutive ratings ≤ %d detected", maxConsecutiveLow, LOW_RATING_VALUE));
        if (negativeSentiment && !uniqueNegativeKeywords.isEmpty()) {
            reasons.add("Negative feedback keywords: '" + String.join(", ", uniqueNegativeKeywords) + "'");
        }

        boolean shouldFlag = avgLow || consecutiveLow || negativeSentiment;

        // --- Save Analysis Log ---
        RatingAnalysisLog log = new RatingAnalysisLog();
        log.setId("log-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        log.setUserId(userId);
        log.setAvgRating(avgRating);
        log.setTotalReviews(subjectReviews.size());
        log.setConsecutiveLowRatings(maxConsecutiveLow);
        log.setSentimentScore(avgSentimentScore);
        log.setNegativeKeywords(String.join(", ", uniqueNegativeKeywords));
        log.setFlagged(shouldFlag);
        log.setFlagReason(reasons.isEmpty() ? null : String.join(" + ", reasons));
        log.setAnalyzedAt(LocalDateTime.now());
        analysisLogRepo.save(log);

        // --- Flag User: Duplicate Check (don't re-add if already PENDING) ---
        if (shouldFlag) {
            boolean alreadyPending = blacklistRepo.existsByUserIdAndStatus(
                userId, BlacklistCandidate.CandidateStatus.PENDING);
            if (!alreadyPending) {
                BlacklistCandidate candidate = new BlacklistCandidate();
                candidate.setId("bc-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                candidate.setUserId(userId);
                candidate.setUserName(user.getFirstName() + " " + user.getLastName());
                candidate.setUserEmail(user.getEmail());
                candidate.setUserRole(user.getRole().name());
                candidate.setAvgRating(avgRating);
                candidate.setTotalReviews(subjectReviews.size());
                candidate.setConsecutiveLowRatings(maxConsecutiveLow);
                candidate.setAiReason(String.join(" + ", reasons));
                candidate.setStatus(BlacklistCandidate.CandidateStatus.PENDING);
                candidate.setCreatedAt(LocalDateTime.now());
                blacklistRepo.save(candidate);
            }
        }
    }

    /**
     * Calculate the maximum number of consecutive reviews with rating <= LOW_RATING_VALUE.
     */
    private int calculateMaxConsecutiveLow(List<Review> reviews) {
        int max = 0;
        int current = 0;
        for (Review r : reviews) {
            if (r.getRating() <= LOW_RATING_VALUE) {
                current++;
                max = Math.max(max, current);
            } else {
                current = 0;
            }
        }
        return max;
    }
}
