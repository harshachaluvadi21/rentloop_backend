package com.rentloop.controller;

import com.rentloop.entity.Item;
import com.rentloop.entity.Rental;
import com.rentloop.repository.ItemRepository;
import com.rentloop.repository.RentalRepository;
import com.rentloop.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final RentalRepository rentalRepo;

    public PublicController(UserRepository userRepo, ItemRepository itemRepo, RentalRepository rentalRepo) {
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.rentalRepo = rentalRepo;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalItems", itemRepo.countByStatusAndApprovedTrue(Item.Status.available));
        stats.put("totalUsers", userRepo.count());
        stats.put("completedRentals", rentalRepo.countByStatus(Rental.Status.completed));
        return ResponseEntity.ok(stats);
    }
}
