package com.datn.datnbe.ai.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIResultResponseDto {
    Integer id;
    String result;
    String createdAt;
    String presentationId;
}
