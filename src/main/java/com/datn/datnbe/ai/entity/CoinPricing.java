package com.datn.datnbe.ai.entity;

import com.datn.datnbe.ai.enums.ResourceType;
import com.datn.datnbe.ai.enums.UnitType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "coin_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "coin_pricing", uniqueConstraints = {
        @UniqueConstraint(name = "uk_coin_pricing_resource_model", columnNames = {"resource_type",
                "model_name"})}, indexes = {
                        @Index(name = "idx_coin_pricing_resource_type", columnList = "resource_type"),
                        @Index(name = "idx_coin_pricing_model_name", columnList = "model_name")})
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

    @Column(name = "model_name", length = 100)
    String modelName;

    @Column(name = "base_cost", nullable = false)
    Integer baseCost;

    @Column(name = "unit_type", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    UnitType unitType = UnitType.PER_REQUEST;

    @Column(name = "unit_multiplier", precision = 10, scale = 2)
    @Builder.Default
    BigDecimal unitMultiplier = BigDecimal.ONE;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
