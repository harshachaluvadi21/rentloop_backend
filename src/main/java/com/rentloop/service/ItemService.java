package com.rentloop.service;

import com.rentloop.entity.Item;
import com.rentloop.entity.User;
import com.rentloop.repository.ItemRepository;
import com.rentloop.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemRepository itemRepo;
    private final ReviewRepository reviewRepo;

    public ItemService(ItemRepository itemRepo, ReviewRepository reviewRepo) {
        this.itemRepo = itemRepo;
        this.reviewRepo = reviewRepo;
    }

    public List<Map<String, Object>> browseItems(String query, String category) {
        List<Item> items = itemRepo.findByStatusAndApprovedTrue(Item.Status.available);
        if (query != null && !query.isEmpty()) {
            String q = query.toLowerCase();
            items = items.stream().filter(i ->
                i.getName().toLowerCase().contains(q) ||
                i.getCategory().toLowerCase().contains(q) ||
                (i.getLocation() != null && i.getLocation().toLowerCase().contains(q))
            ).collect(Collectors.toList());
        }
        if (category != null && !category.equals("all")) {
            items = items.stream().filter(i -> i.getCategory().equals(category)).collect(Collectors.toList());
        }
        return items.stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getOwnerItems(String ownerId) {
        return itemRepo.findByOwnerId(ownerId).stream().map(this::toMap).collect(Collectors.toList());
    }

    public Map<String, Object> getItem(String id) {
        Item item = itemRepo.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        return toMap(item);
    }

    public Map<String, Object> createItem(Map<String, Object> body, String ownerId) {
        Item item = new Item();
        item.setId("item" + UUID.randomUUID().toString().replace("-","").substring(0,10));
        item.setOwnerId(ownerId);
        item.setName((String) body.get("name"));
        item.setDescription((String) body.get("description"));
        item.setCategory((String) body.get("category"));
        item.setEmoji(body.getOrDefault("emoji", "📦").toString());
        item.setPrice(new BigDecimal(body.get("price").toString()));
        item.setUnit(body.getOrDefault("unit", "day").toString());
        item.setLocation((String) body.get("location"));
        item.setStatus(Item.Status.available);
        item.setApproved(false);
        item.setImages(body.containsKey("images") ? body.get("images").toString() : "[]");
        item.setSerialNumber((String) body.get("serialNumber"));
        item.setBrandModel((String) body.get("brandModel"));
        item.setInvoiceNo((String) body.get("invoiceNo"));
        item.setCondition((String) body.get("condition"));
        item.setPurchaseYear((String) body.get("purchaseYear"));
        item.setDamage((String) body.get("damage"));
        return toMap(itemRepo.save(item));
    }

    public Map<String, Object> updateItem(String id, Map<String, Object> body, String ownerId) {
        Item item = itemRepo.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        if (!item.getOwnerId().equals(ownerId)) throw new RuntimeException("Unauthorized");
        if (body.containsKey("name")) item.setName((String) body.get("name"));
        if (body.containsKey("description")) item.setDescription((String) body.get("description"));
        if (body.containsKey("price")) item.setPrice(new BigDecimal(body.get("price").toString()));
        if (body.containsKey("location")) item.setLocation((String) body.get("location"));
        if (body.containsKey("status")) item.setStatus(Item.Status.valueOf((String) body.get("status")));
        if (body.containsKey("serialNumber")) item.setSerialNumber((String) body.get("serialNumber"));
        if (body.containsKey("brandModel")) item.setBrandModel((String) body.get("brandModel"));
        if (body.containsKey("invoiceNo")) item.setInvoiceNo((String) body.get("invoiceNo"));
        if (body.containsKey("condition")) item.setCondition((String) body.get("condition"));
        if (body.containsKey("purchaseYear")) item.setPurchaseYear((String) body.get("purchaseYear"));
        if (body.containsKey("damage")) item.setDamage((String) body.get("damage"));
        return toMap(itemRepo.save(item));
    }

    public void deleteItem(String id, String ownerId, boolean isAdmin) {
        Item item = itemRepo.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        if (!isAdmin && !item.getOwnerId().equals(ownerId)) throw new RuntimeException("Unauthorized");
        itemRepo.delete(item);
    }

    public Map<String, Object> approveItem(String id) {
        Item item = itemRepo.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        item.setApproved(true);
        return toMap(itemRepo.save(item));
    }

    public List<Map<String, Object>> getPendingItems() {
        return itemRepo.findByApprovedFalse().stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllItems() {
        return itemRepo.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    private Map<String, Object> toMap(Item i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("ownerId", i.getOwnerId());
        m.put("name", i.getName());
        m.put("description", i.getDescription());
        m.put("category", i.getCategory());
        m.put("emoji", i.getEmoji());
        m.put("price", i.getPrice());
        m.put("unit", i.getUnit());
        m.put("location", i.getLocation());
        m.put("status", i.getStatus().name());
        m.put("approved", i.getApproved());
        m.put("images", i.getImages() != null ? i.getImages() : "[]");
        m.put("serialNumber", i.getSerialNumber());
        m.put("brandModel", i.getBrandModel());
        m.put("invoiceNo", i.getInvoiceNo());
        m.put("condition", i.getCondition());
        m.put("purchaseYear", i.getPurchaseYear());
        m.put("damage", i.getDamage());
        m.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : "");
        // Average rating
        var revs = reviewRepo.findByItemId(i.getId());
        double avg = revs.isEmpty() ? 0 : revs.stream().mapToInt(r -> r.getRating()).average().orElse(0);
        m.put("avgRating", Math.round(avg * 10.0) / 10.0);
        m.put("reviewCount", revs.size());
        return m;
    }
}
