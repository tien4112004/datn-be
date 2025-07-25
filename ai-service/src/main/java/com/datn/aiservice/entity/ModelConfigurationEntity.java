package com.datn.aiservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity(name = "model_configuration")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigurationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer modelId;
    @Column(name = "model_name", nullable = false, unique = true)
    String modelName;
    @Column(name = "display_name", nullable = false)
    String displayName;
    @Column(name = "is_enabled", nullable = false)
    boolean isEnabled;
    @Column(name = "is_default", nullable = false)
    boolean isDefault;
}
