package com.datn.datnbe.student.exam.entity.valueobject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = StandaloneQuestionOrderItem.class, name = "question"),
        @JsonSubTypes.Type(value = ContextGroupOrderItem.class, name = "context_group")})
public abstract class QuestionOrderItem {
    private String type;
}
