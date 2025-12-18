package com.datn.datnbe.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassResponseDto {

    String id;
    String ownerId;
    String name;
    String description;
    String joinCode;
    String settings;
    Boolean isActive;
    SeatingLayoutResponseDto layout;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
