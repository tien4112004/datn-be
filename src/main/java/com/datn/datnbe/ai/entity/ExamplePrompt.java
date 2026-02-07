package com.datn.datnbe.ai.entity;

import com.datn.datnbe.ai.enums.ExamplePromptType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "example_prompts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamplePrompt {

    @Id
    @UuidGenerator
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "prompt", columnDefinition = "TEXT", nullable = false)
    String prompt;

    @Column(name = "icon", nullable = false)
    String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    ExamplePromptType type;

    @Column(name = "language", length = 10)
    @Builder.Default
    String language = "vi";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "JSONB")
    String data; // Changed to String to store JSON string, or could be Map/Object depending on serializer
}
