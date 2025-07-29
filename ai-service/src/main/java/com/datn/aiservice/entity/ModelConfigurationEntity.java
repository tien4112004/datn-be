package com.datn.aiservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.UniqueElements;

@Entity(name = "model_configuration")
@Getter
@Setter
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
    @Column(name = "is_enabled", nullable = false, columnDefinition = "boolean default true")
    boolean isEnabled;
    @Column(name = "is_default", nullable = false, columnDefinition = "boolean default false")
    boolean isDefault;
    @Column(name = "max_tokens", nullable = false)
    Long maxTokens;
}
