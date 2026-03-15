package com.rentloop.controller;

import com.rentloop.entity.*;
import com.rentloop.repository.*;
import com.rentloop.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepo;
    private final ItemService itemService;
    private final RentalService rentalService;
    private final ReviewService reviewService;
    private final AnnouncementRepository annRepo;
    private final RentalRepository rentalRepo;
    private final ItemRepository itemRepo;
    private final ReviewRepository reviewRepo;

    public AdminController(UserRepository userRepo, ItemService itemService, RentalService rentalService,
                           ReviewService reviewService, AnnouncementRepository annRepo,
                           RentalRepository rentalRepo, ItemRepository itemRepo, ReviewRepository reviewRepo) {
        this.userRepo = userRepo; this.itemService = itemService; this.rentalService = rentalService;
        this.reviewService = reviewService; this.annRepo = annRepo;
        this.rentalRepo = rentalRepo; this.itemRepo = itemRepo; this.reviewRepo = reviewRepo;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepo.count());
        stats.put("totalItems", itemRepo.count());
        stats.put("totalRentals", rentalRepo.count());
        stats.put("pendingApprovals", itemRepo.countByApprovedFalse());
        stats.put("activeRentals", rentalRepo.countByStatus(Rental.Status.approved));
        stats.put("pendingRentals", rentalRepo.countByStatus(Rental.Status.pending));
        stats.put("owners", userRepo.countByRole(User.Role.owner));
        stats.put("renters", userRepo.countByRole(User.Role.renter));
        double totalRevenue = rentalRepo.findByStatus(Rental.Status.completed).stream()
                .mapToDouble(r -> r.getTotal().doubleValue()).sum();
        stats.put("totalRevenue", totalRevenue);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        return ResponseEntity.ok(userRepo.findAll().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",u.getId()); m.put("email",u.getEmail());
            m.put("firstName",u.getFirstName()); m.put("lastName",u.getLastName());
            m.put("phone",u.getPhone()); m.put("role",u.getRole().name());
            m.put("location",u.getLocation()); m.put("color",u.getColor());
            m.put("status",u.getStatus().name());
            m.put("joinedDate",u.getJoinedDate()!=null?u.getJoinedDate().toString():"");
            long itemCount = itemRepo.findByOwnerId(u.getId()).size();
            long rentalCount = rentalRepo.findByRenterId(u.getId()).size();
            m.put("itemCount",itemCount); m.put("rentalCount",rentalCount);
            return m;
        }).collect(Collectors.toList()));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable String id, @RequestBody Map<String,String> body) {
        return userRepo.findById(id).map(u -> {
            u.setStatus(User.Status.valueOf(body.get("status")));
            return ResponseEntity.ok((Object)userRepo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message","Deleted"));
    }

    @GetMapping("/items")
    public ResponseEntity<?> items() { return ResponseEntity.ok(itemService.getAllItems()); }

    @GetMapping("/items/pending")
    public ResponseEntity<?> pendingItems() { return ResponseEntity.ok(itemService.getPendingItems()); }

    @PatchMapping("/items/{id}/approve")
    public ResponseEntity<?> approveItem(@PathVariable String id) {
        return ResponseEntity.ok(itemService.approveItem(id));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable String id, @AuthenticationPrincipal User user) {
        itemService.deleteItem(id, user.getId(), true);
        return ResponseEntity.ok(Map.of("message","Deleted"));
    }

    @GetMapping("/rentals")
    public ResponseEntity<?> rentals() { return ResponseEntity.ok(rentalService.getAllRentals()); }

    @GetMapping("/reviews")
    public ResponseEntity<?> reviews() { return ResponseEntity.ok(reviewService.getAllReviews()); }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id); return ResponseEntity.ok(Map.of("message","Deleted"));
    }

    @GetMapping("/announcements")
    public ResponseEntity<?> getAnnouncements() {
        return ResponseEntity.ok(annRepo.findAllByOrderByCreatedAtDesc().stream().map(a -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id",a.getId()); m.put("title",a.getTitle()); m.put("body",a.getBody());
            m.put("type",a.getType().name()); m.put("author",a.getAuthor());
            m.put("createdAt",a.getCreatedAt()!=null?a.getCreatedAt().toLocalDate().toString():"");
            return m;
        }).collect(Collectors.toList()));
    }

    @PostMapping("/announcements")
    public ResponseEntity<?> postAnnouncement(@RequestBody Map<String,String> body, @AuthenticationPrincipal User user) {
        Announcement a = new Announcement();
        a.setId("ann"+UUID.randomUUID().toString().replace("-","").substring(0,10));
        a.setTitle(body.get("title")); a.setBody(body.get("body"));
        a.setType(Announcement.Type.valueOf(body.getOrDefault("type","info")));
        a.setAuthor(user.getFirstName()+" "+user.getLastName());
        annRepo.save(a);
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id",a.getId()); m.put("title",a.getTitle()); m.put("body",a.getBody());
        m.put("type",a.getType().name()); m.put("author",a.getAuthor());
        m.put("createdAt",a.getCreatedAt().toLocalDate().toString());
        return ResponseEntity.ok(m);
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable String id) {
        annRepo.deleteById(id); return ResponseEntity.ok(Map.of("message","Deleted"));
    }
}
