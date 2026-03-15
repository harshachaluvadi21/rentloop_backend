package com.rentloop.controller;

import com.rentloop.repository.AnnouncementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {
    private final AnnouncementRepository annRepo;
    public AnnouncementController(AnnouncementRepository annRepo) { this.annRepo = annRepo; }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(annRepo.findAllByOrderByCreatedAtDesc().stream().map(a -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id",a.getId()); m.put("title",a.getTitle()); m.put("body",a.getBody());
            m.put("type",a.getType().name()); m.put("author",a.getAuthor());
            m.put("createdAt",a.getCreatedAt()!=null?a.getCreatedAt().toLocalDate().toString():"");
            return m;
        }).collect(Collectors.toList()));
    }
}
