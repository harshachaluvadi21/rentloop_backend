package com.rentloop.repository;

import com.rentloop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByItemId(String itemId);
    List<Review> findByRenterId(String renterId);
    List<Review> findByOwnerId(String ownerId);
    List<Review> findByItemIdIn(List<String> itemIds);
    Optional<Review> findByRentalId(String rentalId);
    boolean existsByRentalId(String rentalId);
    boolean existsByRentalIdAndReviewerId(String rentalId, String reviewerId);
}
