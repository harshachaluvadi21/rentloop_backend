package com.rentloop.controller;

import com.rentloop.entity.User;
import com.rentloop.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService itemService;
    public ItemController(ItemService itemService) { this.itemService = itemService; }

    @GetMapping("/browse")
    public ResponseEntity<?> browse(@RequestParam(required=false) String query,
                                    @RequestParam(required=false,defaultValue="all") String category) {
        return ResponseEntity.ok(itemService.browseItems(query, category));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itemService.getOwnerItems(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItem(@PathVariable String id) {
        try { return ResponseEntity.ok(itemService.getItem(id)); }
        catch (RuntimeException e) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, @AuthenticationPrincipal User user) {
        try { return ResponseEntity.ok(itemService.createItem(body, user.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal User user) {
        try { return ResponseEntity.ok(itemService.updateItem(id, body, user.getId())); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, @AuthenticationPrincipal User user) {
        try {
            boolean isAdmin = user.getRole() == com.rentloop.entity.User.Role.admin;
            itemService.deleteItem(id, user.getId(), isAdmin);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }
}
