package com.rentloop.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@NoArgsConstructor
public class Review {
    @Id
    private String id;

    private String rentalId;

    private String itemId;

    private String ownerId;

    private String renterId;

    private String reviewerId;

    private Integer rating;

    private String comment;

    private LocalDate reviewDate = LocalDate.now();

    private LocalDateTime createdAt = LocalDateTime.now();
}
