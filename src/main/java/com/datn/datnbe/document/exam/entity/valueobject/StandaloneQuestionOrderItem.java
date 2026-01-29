package com.datn.datnbe.document.exam.entity.valueobject;

import lombok.*;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandaloneQuestionOrderItem extends QuestionOrderItem {
    @Builder.Default
    private String type = "question";
    private UUID questionId;
    private Integer points;
}
