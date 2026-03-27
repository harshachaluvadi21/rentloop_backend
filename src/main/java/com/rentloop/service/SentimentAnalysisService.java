package com.rentloop.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight rule-based sentiment analysis service.
 * Analyzes feedback text for negative sentiment without heavy ML models.
 */
@Service
public class SentimentAnalysisService {

    // Negative keyword dictionary for rental context
    private static final List<String> NEGATIVE_KEYWORDS = List.of(
        "scam", "fraud", "broken", "damaged", "terrible", "horrible", "awful",
        "worst", "bad", "dirty", "disgusting", "useless", "defective", "fake",
        "cheat", "liar", "rude", "irresponsible", "unreliable", "dangerous",
        "missing", "incomplete", "late", "delay", "refused", "stolen",
        "pathetic", "garbage", "trash", "waste", "unacceptable", "bogus",
        "non-functional", "not working", "never returned", "no response"
    );

    public static class SentimentResult {
        public final double score; // 0.0 = very negative, 1.0 = very positive
        public final List<String> detectedKeywords;
        public final String label; // NEGATIVE, NEUTRAL, POSITIVE

        public SentimentResult(double score, List<String> detectedKeywords, String label) {
            this.score = score;
            this.detectedKeywords = detectedKeywords;
            this.label = label;
        }
    }

    /**
     * Analyze the given feedback text and return a SentimentResult.
     * Handles null/blank input gracefully by returning NEUTRAL.
     */
    public SentimentResult analyze(String text) {
        if (text == null || text.isBlank()) {
            return new SentimentResult(0.5, new ArrayList<>(), "NEUTRAL");
        }

        String lowerText = text.toLowerCase();
        List<String> found = new ArrayList<>();

        for (String keyword : NEGATIVE_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                found.add(keyword);
            }
        }

        // Score decreases with each negative keyword found
        double score = Math.max(0.0, 1.0 - (found.size() * 0.2));
        String label;
        if (score < 0.4) {
            label = "NEGATIVE";
        } else if (score < 0.7) {
            label = "NEUTRAL";
        } else {
            label = "POSITIVE";
        }

        return new SentimentResult(score, found, label);
    }
}
