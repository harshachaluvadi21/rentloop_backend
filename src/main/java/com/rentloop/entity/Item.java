package com.rentloop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
public class Item {
    @Id
    private String id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    private String emoji = "📦";

    @Column(nullable = false)
    private BigDecimal price;

    private String unit = "day";
    private String location;

    @Enumerated(EnumType.STRING)
    private Status status = Status.available;

    private Boolean approved = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images")
    private String images;


    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "brand_model")
    private String brandModel;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "item_condition")
    private String condition;

    @Column(name = "purchase_year")
    private String purchaseYear;

    @Column(columnDefinition = "TEXT")
    private String damage;


    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        available, rented, unavailable
    }
}
