package com.rentloop.controller;

import com.rentloop.entity.User;
import com.rentloop.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo; this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toMap(user));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body, @AuthenticationPrincipal User user) {
        String phone = (String) body.get("phone");
        if (phone != null && !phone.isEmpty() && userRepo.existsByPhone(phone) && !phone.equals(user.getPhone()))
            return ResponseEntity.badRequest().body(Map.of("error", "Phone number already in use"));
        if (body.containsKey("firstName")) user.setFirstName((String) body.get("firstName"));
        if (body.containsKey("lastName")) user.setLastName((String) body.get("lastName"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("location")) user.setLocation((String) body.get("location"));
        if (phone != null) user.setPhone(phone);
        String pw = (String) body.get("password");
        if (pw != null && !pw.isEmpty()) user.setPassword(passwordEncoder.encode(pw));
        return ResponseEntity.ok(toMap(userRepo.save(user)));
    }

    private Map<String, Object> toMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId()); m.put("email", u.getEmail());
        m.put("firstName", u.getFirstName()); m.put("lastName", u.getLastName());
        m.put("phone", u.getPhone()); m.put("role", u.getRole().name());
        m.put("location", u.getLocation()); m.put("color", u.getColor());
        m.put("status", u.getStatus().name());
        m.put("joinedDate", u.getJoinedDate() != null ? u.getJoinedDate().toString() : "");
        return m;
    }
}
