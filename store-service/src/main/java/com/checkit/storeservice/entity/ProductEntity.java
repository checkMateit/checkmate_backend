package com.checkit.storeservice.entity;

import com.checkit.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity extends AuditBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean isAvailable = true;

    @Builder
    public ProductEntity(String name, String category, int price, boolean isAvailable) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    public void update(String name, String category, Integer price, Boolean isAvailable) {
        if (name != null) this.name = name;
        if (category != null) this.category = category;
        if (price != null) this.price = price;
        if (isAvailable != null) this.isAvailable = isAvailable;
    }

    @Override
    public void softDelete(java.util.UUID actorId) {
        super.softDelete(actorId);
        this.isAvailable = false;
    }
}
