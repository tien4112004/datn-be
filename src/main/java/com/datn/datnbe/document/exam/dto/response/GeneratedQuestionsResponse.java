package com.datn.datnbe.document.exam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedQuestionsResponse {
    private List<UUID> questionIds;
    private Integer totalGenerated;
    private String message;
}
