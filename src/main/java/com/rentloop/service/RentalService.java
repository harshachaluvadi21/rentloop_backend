package com.rentloop.service;

import com.rentloop.entity.Item;
import com.rentloop.entity.Rental;
import com.rentloop.entity.User;
import com.rentloop.repository.ItemRepository;
import com.rentloop.repository.RentalRepository;
import com.rentloop.repository.UserRepository;
import com.rentloop.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RentalService {
    private final RentalRepository rentalRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
    private final ReviewRepository reviewRepo;

    public RentalService(RentalRepository rentalRepo, ItemRepository itemRepo, UserRepository userRepo, ReviewRepository reviewRepo) {
        this.rentalRepo = rentalRepo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
    }

    public Map<String, Object> createRental(Map<String, Object> body, String renterId) {
        String itemId = (String) body.get("itemId");
        String startStr = (String) body.get("startDate");
        String endStr = (String) body.get("endDate");

        Item item = itemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getStatus().equals(Item.Status.available)) throw new RuntimeException("Item not available");
        if (!item.getApproved()) throw new RuntimeException("Item not approved");

        LocalDate start = LocalDate.parse(startStr);
        LocalDate end = LocalDate.parse(endStr);
        if (!end.isAfter(start)) throw new RuntimeException("End date must be after start date");

        long days = ChronoUnit.DAYS.between(start, end);
        BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(days));

        Rental rental = new Rental();
        rental.setId("r" + UUID.randomUUID().toString().replace("-","").substring(0,12));
        rental.setItemId(itemId);
        rental.setRenterId(renterId);
        rental.setOwnerId(item.getOwnerId());
        rental.setStartDate(start);
        rental.setEndDate(end);
        rental.setDays((int) days);
        rental.setTotal(total);
        rental.setStatus(Rental.Status.pending);
        if (body.containsKey("message")) rental.setMessage((String) body.get("message"));

        return toMap(rentalRepo.save(rental));
    }

    public List<Map<String, Object>> getRenterBookings(String renterId) {
        return rentalRepo.findByRenterId(renterId).stream()
                .sorted(Comparator.comparing(Rental::getCreatedAt).reversed())
                .map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getOwnerRequests(String ownerId) {
        List<String> myItemIds = itemRepo.findByOwnerId(ownerId).stream()
                .map(Item::getId).collect(Collectors.toList());
        return rentalRepo.findByItemIdIn(myItemIds).stream()
                .sorted(Comparator.comparing(Rental::getCreatedAt).reversed())
                .map(this::toMap).collect(Collectors.toList());
    }

    public Map<String, Object> updateStatus(String id, String status, String userId) {
        Rental rental = rentalRepo.findById(id).orElseThrow(() -> new RuntimeException("Rental not found"));
        rental.setStatus(Rental.Status.valueOf(status));

        // Mark item as rented/available based on status
        Item item = itemRepo.findById(rental.getItemId()).orElse(null);
        if (item != null) {
            if (status.equals("approved")) item.setStatus(Item.Status.rented);
            else if (status.equals("rejected") || status.equals("cancelled") || status.equals("completed")) {
                item.setStatus(Item.Status.available);
            }
            itemRepo.save(item);
        }

        return toMap(rentalRepo.save(rental));
    }

    public Map<String, Object> markPickup(String id, boolean pickedUp) {
        Rental rental = rentalRepo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        rental.setPickedUp(pickedUp);
        return toMap(rentalRepo.save(rental));
    }

    public Map<String, Object> markReturn(String id) {
        Rental rental = rentalRepo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        rental.setReturned(true);
        rental.setStatus(Rental.Status.completed);
        Item item = itemRepo.findById(rental.getItemId()).orElse(null);
        if (item != null) { item.setStatus(Item.Status.available); itemRepo.save(item); }
        return toMap(rentalRepo.save(rental));
    }

    public List<Map<String, Object>> getAllRentals() {
        return rentalRepo.findAll().stream()
                .sorted(Comparator.comparing(Rental::getCreatedAt).reversed())
                .map(this::toMap).collect(Collectors.toList());
    }

    private Map<String, Object> toMap(Rental r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("itemId", r.getItemId());
        m.put("renterId", r.getRenterId());
        m.put("ownerId", r.getOwnerId());
        m.put("startDate", r.getStartDate().toString());
        m.put("endDate", r.getEndDate().toString());
        m.put("days", r.getDays());
        m.put("total", r.getTotal());
        m.put("status", r.getStatus().name());
        m.put("pickedUp", r.getPickedUp());
        m.put("returned", r.getReturned());
        m.put("message", r.getMessage() != null ? r.getMessage() : "");
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");

        // Enrich with item and user data
        itemRepo.findById(r.getItemId()).ifPresent(item -> {
            m.put("itemName", item.getName());
            m.put("itemEmoji", item.getEmoji());
            m.put("itemCategory", item.getCategory());
        });
        userRepo.findById(r.getRenterId()).ifPresent(u -> {
            m.put("renterName", u.getFirstName() + " " + u.getLastName());
            m.put("renterColor", u.getColor());
        });
        userRepo.findById(r.getOwnerId()).ifPresent(u -> {
            m.put("ownerName", u.getFirstName() + " " + u.getLastName());
        });
        m.put("isRenterReviewed", reviewRepo.existsByRentalIdAndReviewerId(r.getId(), r.getRenterId()));
        m.put("isOwnerReviewed", reviewRepo.existsByRentalIdAndReviewerId(r.getId(), r.getOwnerId()));
        return m;
    }
}
