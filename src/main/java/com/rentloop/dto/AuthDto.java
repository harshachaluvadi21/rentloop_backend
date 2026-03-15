package com.rentloop.dto;

import lombok.Data;

public class AuthDto {
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String password;
        private String role;
        private String location;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private UserDto user;
    }

    @Data
    public static class UserDto {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String role;
        private String location;
        private String color;
        private String status;
        private String joinedDate;
    }
}
