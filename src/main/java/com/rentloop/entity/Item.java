package com.rentloop.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "items")
@Data
@NoArgsConstructor
public class Item {
    @Id
    private String id;

    private String ownerId;

    private String name;

    private String description;

    private String category;

    private String emoji = "📦";

    private BigDecimal price;

    private String unit = "day";
    private String location;

    private Status status = Status.available;

    private Boolean approved = false;

    private String images;


    private String serialNumber;

    private String brandModel;

    private String invoiceNo;

    private String condition;

    private String purchaseYear;

    private String damage;


    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        available, rented, unavailable
    }
}
