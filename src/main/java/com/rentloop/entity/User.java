package com.rentloop.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private String phone;

    private Role role = Role.renter;

    private String location = "Hyderabad, TS";
    private String color = "#F07C2B";

    private Status status = Status.active;

    private LocalDate joinedDate = LocalDate.now();

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role { owner, renter, admin }
    public enum Status { active, verified, suspended }
}
