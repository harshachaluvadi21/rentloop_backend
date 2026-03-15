package com.rentloop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Data
@NoArgsConstructor
public class Announcement {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    private Type type = Type.info;

    private String author = "Admin";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Type { info, warning, success }
}
