package com.datn.datnbe.ai.entity;

import com.datn.datnbe.ai.enums.ModelType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity(name = "model_configuration")
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk_model_name_type", columnNames = {"model_name", "model_type"})})
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigurationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer modelId;

    @Column(name = "model_name", nullable = false)
    String modelName;

    @Column(name = "display_name", nullable = false)
    String displayName;

    @Column(name = "is_enabled", nullable = false, columnDefinition = "boolean default true")
    boolean isEnabled;

    @Column(name = "is_default", nullable = false, columnDefinition = "boolean default false")
    boolean isDefault;

    @Column(name = "model_type", nullable = false)
    @Enumerated(EnumType.STRING)
    ModelType modelType;

    @Column(name = "provider", nullable = false)
    String provider;
}
