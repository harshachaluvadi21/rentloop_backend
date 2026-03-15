package com.rentloop.controller;

import com.rentloop.entity.User;
import com.rentloop.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) { this.reviewService = reviewService; }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, @AuthenticationPrincipal User user) {
        try { return ResponseEntity.ok(reviewService.createReview(body, user.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> itemReviews(@PathVariable String itemId) {
        return ResponseEntity.ok(reviewService.getItemReviews(itemId));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myReviews(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.getMyReviews(user.getId(), user.getRole().name()));
    }
}
