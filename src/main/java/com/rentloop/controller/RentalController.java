package com.rentloop.controller;

import com.rentloop.entity.User;
import com.rentloop.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {
    private final RentalService rentalService;
    public RentalController(RentalService rentalService) { this.rentalService = rentalService; }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, @AuthenticationPrincipal User user) {
        try { return ResponseEntity.ok(rentalService.createRental(body, user.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<?> myBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(rentalService.getRenterBookings(user.getId()));
    }

    @GetMapping("/requests")
    public ResponseEntity<?> ownerRequests(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(rentalService.getOwnerRequests(user.getId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body,
                                          @AuthenticationPrincipal User user) {
        try { return ResponseEntity.ok(rentalService.updateStatus(id, body.get("status"), user.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PatchMapping("/{id}/pickup")
    public ResponseEntity<?> markPickup(@PathVariable String id, @RequestBody Map<String, Boolean> body,
                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(rentalService.markPickup(id, body.getOrDefault("pickedUp", true)));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<?> markReturn(@PathVariable String id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(rentalService.markReturn(id));
    }
}
