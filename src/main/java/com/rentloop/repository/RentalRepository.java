package com.rentloop.repository;

import com.rentloop.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, String> {
    List<Rental> findByRenterId(String renterId);
    List<Rental> findByOwnerId(String ownerId);
    List<Rental> findByItemId(String itemId);
    List<Rental> findByItemIdIn(List<String> itemIds);
    List<Rental> findByStatus(Rental.Status status);
    long countByStatus(Rental.Status status);
}
