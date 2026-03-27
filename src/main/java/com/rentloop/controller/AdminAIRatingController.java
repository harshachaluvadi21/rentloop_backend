package com.rentloop.controller;

import com.rentloop.entity.*;
import com.rentloop.repository.*;
import com.rentloop.service.RatingAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for the AI Rating Monitoring feature.
 * All endpoints are additive and do NOT modify any existing APIs.
 * Base path: /api/admin/ai
 */
@RestController
@RequestMapping("/api/admin/ai")
public class AdminAIRatingController {

    private final BlacklistCandidateRepository blacklistRepo;
    private final UserRepository userRepo;
    private final RatingAnalysisService ratingAnalysisService;

    public AdminAIRatingController(BlacklistCandidateRepository blacklistRepo,
                                    UserRepository userRepo,
                                    RatingAnalysisService ratingAnalysisService) {
        this.blacklistRepo = blacklistRepo;
        this.userRepo = userRepo;
        this.ratingAnalysisService = ratingAnalysisService;
    }

    /**
     * GET /api/admin/ai/blacklist-candidates
     * Returns all flagged users (all statuses or filtered by status query param).
     */
    @GetMapping("/blacklist-candidates")
    public ResponseEntity<?> getBlacklistCandidates(
            @RequestParam(required = false, defaultValue = "PENDING") String status) {
        List<BlacklistCandidate> candidates;
        try {
            BlacklistCandidate.CandidateStatus candidateStatus =
                BlacklistCandidate.CandidateStatus.valueOf(status.toUpperCase());
            candidates = blacklistRepo.findByStatusOrderByCreatedAtDesc(candidateStatus);
        } catch (IllegalArgumentException e) {
            candidates = blacklistRepo.findAllByOrderByCreatedAtDesc();
        }

        List<Map<String, Object>> result = candidates.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("userId", c.getUserId());
            m.put("userName", c.getUserName());
            m.put("userEmail", c.getUserEmail());
            m.put("userRole", c.getUserRole());
            m.put("avgRating", c.getAvgRating());
            m.put("totalReviews", c.getTotalReviews());
            m.put("consecutiveLowRatings", c.getConsecutiveLowRatings());
            m.put("aiReason", c.getAiReason());
            m.put("status", c.getStatus().name());
            m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
            m.put("resolvedAt", c.getResolvedAt() != null ? c.getResolvedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/admin/ai/suspend-user/{id}
     * Approves the blacklist flag → suspends the user.
     */
    @PostMapping("/suspend-user/{id}")
    public ResponseEntity<?> suspendUser(@PathVariable String id) {
        return blacklistRepo.findById(id).map(candidate -> {
            // Update blacklist record
            candidate.setStatus(BlacklistCandidate.CandidateStatus.SUSPENDED);
            candidate.setResolvedAt(LocalDateTime.now());
            blacklistRepo.save(candidate);

            // Suspend the actual user account
            return userRepo.findById(candidate.getUserId()).map(user -> {
                user.setStatus(User.Status.suspended);
                userRepo.save(user);
                return ResponseEntity.ok((Object) Map.of(
                    "message", "User suspended successfully",
                    "userId", candidate.getUserId(),
                    "candidateId", id
                ));
            }).orElse(ResponseEntity.ok(Map.of(
                "message", "Blacklist record updated, but user not found",
                "candidateId", id
            )));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/admin/ai/reject-flag/{id}
     * Rejects the blacklist flag → removes candidate, no action on user.
     */
    @PostMapping("/reject-flag/{id}")
    public ResponseEntity<?> rejectFlag(@PathVariable String id) {
        return blacklistRepo.findById(id).map(candidate -> {
            candidate.setStatus(BlacklistCandidate.CandidateStatus.REJECTED);
            candidate.setResolvedAt(LocalDateTime.now());
            blacklistRepo.save(candidate);
            return ResponseEntity.ok((Object) Map.of(
                "message", "Flag rejected, user cleared",
                "userId", candidate.getUserId(),
                "candidateId", id
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/admin/ai/trigger-analysis
     * Manually triggers the AI analysis (useful for testing and admin debugging).
     */
    @PostMapping("/trigger-analysis")
    public ResponseEntity<?> triggerAnalysis() {
        ratingAnalysisService.analyzeAllUsers();
        return ResponseEntity.ok(Map.of("message", "AI analysis triggered successfully"));
    }

    /**
     * GET /api/admin/ai/stats
     * Returns summary stats for the AI monitoring dashboard.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long pending = blacklistRepo.findByStatusOrderByCreatedAtDesc(
            BlacklistCandidate.CandidateStatus.PENDING).size();
        long suspended = blacklistRepo.findByStatusOrderByCreatedAtDesc(
            BlacklistCandidate.CandidateStatus.SUSPENDED).size();
        long rejected = blacklistRepo.findByStatusOrderByCreatedAtDesc(
            BlacklistCandidate.CandidateStatus.REJECTED).size();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("pendingFlags", pending);
        stats.put("suspendedUsers", suspended);
        stats.put("rejectedFlags", rejected);
        stats.put("totalProcessed", pending + suspended + rejected);
        return ResponseEntity.ok(stats);
    }
}
