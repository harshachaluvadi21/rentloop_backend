package com.rentloop.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "announcements")
@Data
@NoArgsConstructor
public class Announcement {
    @Id
    private String id;

    private String title;

    private String body;

    private Type type = Type.info;

    private String author = "Admin";

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Type { info, warning, success }
}
