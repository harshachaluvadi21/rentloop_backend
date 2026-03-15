package com.rentloop.controller;

import com.rentloop.dto.AuthDto;
import com.rentloop.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest req) {
        try { return ResponseEntity.ok(authService.login(req)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDto.RegisterRequest req) {
        try { return ResponseEntity.ok(authService.register(req)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/seed")
    public ResponseEntity<?> seed() {
        try { return ResponseEntity.ok(Map.of("message", authService.seedDemoData())); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage())); }
    }
}
