package com.rentloop.service;

import com.rentloop.dto.AuthDto;
import com.rentloop.entity.User;
import com.rentloop.repository.UserRepository;
import com.rentloop.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final List<String> COLORS = List.of(
        "#F07C2B","#2E7D4F","#2563A8","#5B3FA6","#1D9E75","#D4537E"
    );

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid email or password");
        return buildResponse(user);
    }

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");
        if (req.getPhone() != null && !req.getPhone().isEmpty() && userRepo.existsByPhone(req.getPhone()))
            throw new RuntimeException("Phone number already registered");

        long count = userRepo.count();
        String[] colorArr = COLORS.toArray(new String[0]);

        User user = new User();
        user.setId("u" + UUID.randomUUID().toString().replace("-","").substring(0,12));
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPhone(req.getPhone());
        user.setRole(User.Role.valueOf(req.getRole() != null ? req.getRole() : "renter"));
        user.setLocation(req.getLocation() != null && !req.getLocation().isEmpty() ? req.getLocation() : "Hyderabad, TS");
        user.setColor(colorArr[(int)(count % colorArr.length)]);
        user.setStatus(User.Status.active);
        user.setJoinedDate(LocalDate.now());

        userRepo.save(user);
        return buildResponse(user);
    }

    private AuthDto.AuthResponse buildResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        AuthDto.UserDto dto = new AuthDto.UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setLocation(user.getLocation());
        dto.setColor(user.getColor());
        dto.setStatus(user.getStatus().name());
        dto.setJoinedDate(user.getJoinedDate() != null ? user.getJoinedDate().toString() : "");
        AuthDto.AuthResponse res = new AuthDto.AuthResponse();
        res.setToken(token);
        res.setUser(dto);
        return res;
    }
}
