package com.datn.datnbe.document.entity.valueobject;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextGroupOrderItem extends QuestionOrderItem {
    @Builder.Default
    private String type = "context_group";
    private UUID contextId;
    @Builder.Default
    private List<ContextQuestion> questions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContextQuestion {
        private UUID questionId;
        private Integer points;
    }
}
