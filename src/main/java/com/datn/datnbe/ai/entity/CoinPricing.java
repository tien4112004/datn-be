package com.datn.datnbe.ai.entity;

import com.datn.datnbe.ai.enums.ResourceType;
import com.datn.datnbe.ai.enums.UnitType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Date;

@Entity(name = "coin_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "coin_pricing", uniqueConstraints = {
        @UniqueConstraint(name = "uk_coin_pricing_resource_model", columnNames = {"resource_type",
                "model_id"})}, indexes = {@Index(name = "idx_coin_pricing_resource_type", columnList = "resource_type"),
                        @Index(name = "idx_coin_pricing_model_id", columnList = "model_id")})
@SQLDelete(sql = "UPDATE coin_pricing SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoinPricing {

    @Id
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "resource_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    ResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    ModelConfigurationEntity model;

    @Column(name = "base_cost", nullable = false)
    Integer baseCost;

    @Column(name = "unit_type", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    UnitType unitType = UnitType.PER_REQUEST;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    Date createdAt;

    @Column(name = "updated_at", nullable = false)
    Date updatedAt;

    @Column(name = "deleted_at")
    Date deletedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
