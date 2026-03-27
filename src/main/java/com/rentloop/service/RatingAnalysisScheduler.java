package com.rentloop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to periodically run AI rating analysis.
 * Runs every 6 hours. Safety is handled inside RatingAnalysisService
 * via lastAnalyzedAt timestamp (users with no new reviews are skipped).
 */
@Component
public class RatingAnalysisScheduler {

    private static final Logger log = LoggerFactory.getLogger(RatingAnalysisScheduler.class);

    private final RatingAnalysisService ratingAnalysisService;

    public RatingAnalysisScheduler(RatingAnalysisService ratingAnalysisService) {
        this.ratingAnalysisService = ratingAnalysisService;
    }

    /**
     * Runs every 6 hours.
     * Cron: 0 0 0/6 * * * = every 6 hours starting from midnight
     */
    @Scheduled(cron = "0 0 0/6 * * *")
    public void runPeriodicAnalysis() {
        log.info("[RatingAnalysisScheduler] Starting scheduled AI rating analysis...");
        try {
            ratingAnalysisService.analyzeAllUsers();
            log.info("[RatingAnalysisScheduler] Scheduled analysis completed successfully.");
        } catch (Exception e) {
            log.error("[RatingAnalysisScheduler] Error during scheduled analysis: {}", e.getMessage(), e);
        }
    }
}
