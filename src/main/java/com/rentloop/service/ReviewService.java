package com.rentloop.service;

import com.rentloop.entity.Review;
import com.rentloop.repository.ItemRepository;
import com.rentloop.repository.ReviewRepository;
import com.rentloop.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;

    public ReviewService(ReviewRepository reviewRepo, ItemRepository itemRepo, UserRepository userRepo) {
        this.reviewRepo = reviewRepo; this.itemRepo = itemRepo; this.userRepo = userRepo;
    }

    public Map<String, Object> createReview(Map<String, Object> body, String reviewerId) {
        String rentalId = (String) body.get("rentalId");
        if (reviewRepo.existsByRentalIdAndReviewerId(rentalId, reviewerId)) throw new RuntimeException("You have already reviewed this rental");
        Review review = new Review();
        review.setId("rv" + UUID.randomUUID().toString().replace("-","").substring(0,10));
        review.setRentalId(rentalId);
        review.setItemId((String) body.get("itemId"));
        review.setOwnerId((String) body.get("ownerId"));
        review.setRenterId((String) body.get("renterId"));
        review.setReviewerId(reviewerId);
        review.setRating(Integer.parseInt(body.get("rating").toString()));
        review.setComment((String) body.get("comment"));
        review.setReviewDate(LocalDate.now());
        return toMap(reviewRepo.save(review));
    }

    public List<Map<String, Object>> getItemReviews(String itemId) {
        return reviewRepo.findByItemId(itemId).stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMyReviews(String userId, String role) {
        List<Review> reviews;
        if ("owner".equals(role)) {
            List<String> myItemIds = itemRepo.findByOwnerId(userId).stream()
                    .map(i -> i.getId()).collect(Collectors.toList());
            reviews = reviewRepo.findByItemIdIn(myItemIds);
        } else {
            reviews = reviewRepo.findByRenterId(userId);
        }
        return reviews.stream().sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllReviews() {
        return reviewRepo.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    public void deleteReview(String id) {
        reviewRepo.deleteById(id);
    }

    private Map<String, Object> toMap(Review r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("rentalId", r.getRentalId());
        m.put("itemId", r.getItemId());
        m.put("ownerId", r.getOwnerId());
        m.put("renterId", r.getRenterId());
        m.put("reviewerId", r.getReviewerId());
        m.put("rating", r.getRating());
        m.put("comment", r.getComment());
        m.put("date", r.getReviewDate() != null ? r.getReviewDate().toString() : "");
        itemRepo.findById(r.getItemId()).ifPresent(i -> { m.put("itemName", i.getName()); m.put("itemEmoji", i.getEmoji()); });
        userRepo.findById(r.getRenterId()).ifPresent(u -> { m.put("renterName", u.getFirstName()+" "+u.getLastName()); m.put("renterColor", u.getColor()); });
        return m;
    }
}
