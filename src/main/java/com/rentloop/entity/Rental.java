package com.rentloop.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "rentals")
@Data
@NoArgsConstructor
public class Rental {
    @Id
    private String id;

    private String itemId;

    private String renterId;

    private String ownerId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer days;

    private BigDecimal total;

    private Status status = Status.pending;

    private Boolean pickedUp = false;

    private Boolean returned = false;

    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { pending, approved, rejected, completed, cancelled }
}
