package com.rentloop.repository;

import com.rentloop.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByItemId(String itemId);
    List<Review> findByRenterId(String renterId);
    List<Review> findByOwnerId(String ownerId);
    List<Review> findByItemIdIn(List<String> itemIds);
    Optional<Review> findByRentalId(String rentalId);
    boolean existsByRentalId(String rentalId);
    boolean existsByRentalIdAndReviewerId(String rentalId, String reviewerId);
}
