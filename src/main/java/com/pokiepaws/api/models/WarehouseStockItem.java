package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(
        name = "warehouse_stock_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "warehouse_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseStockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private String name;

    @Column(name = "assortment_description", columnDefinition = "TEXT")
    private String assortmentDescription;

    private Double price;

    private String unit;

    private String category;

    private int amount;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    private String status;
}