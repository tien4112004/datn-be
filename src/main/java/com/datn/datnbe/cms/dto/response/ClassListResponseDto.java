package com.datn.datnbe.cms.dto.response;

import com.datn.datnbe.auth.dto.response.UserMinimalInfoDto;
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
public class ClassListResponseDto {

    String id;
    String ownerId;
    String name;
    String joinCode;
    Boolean isActive;
    UserMinimalInfoDto teacher;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
