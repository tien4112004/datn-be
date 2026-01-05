package com.datn.datnbe.cms.entity;

import com.datn.datnbe.cms.entity.questiondata.*;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class QuestionDeserializer extends StdDeserializer<Question> {

    public QuestionDeserializer() {
        super(Question.class);
    }

    @Override
    public Question deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        String type = node.get("type").asText();
        JsonNode dataNode = node.get("data");

        QuestionData data = null;

        if (dataNode != null) {
            data = switch (type.toUpperCase()) {
                case "MULTIPLE_CHOICE" -> mapper.treeToValue(dataNode, MultipleChoiceData.class);
                case "MATCHING" -> mapper.treeToValue(dataNode, MatchingData.class);
                case "OPEN_ENDED" -> mapper.treeToValue(dataNode, OpenEndedData.class);
                case "FILL_IN_BLANK" -> mapper.treeToValue(dataNode, FillInBlankData.class);
                default -> throw new AppException(ErrorCode.QUESTION_TYPE_MISMATCH, "Unknown question type: " + type);
            };
        }

        return Question.builder()
                .id(node.get("id").asText())
                .type(QuestionType.valueOf(type))
                .difficulty(Difficulty.valueOf(node.get("difficulty").asText()))
                .title(node.get("title").asText())
                .titleImageUrl(node.has("titleImageUrl") ? node.get("titleImageUrl").asText() : null)
                .explanation(node.has("explanation") ? node.get("explanation").asText() : null)
                .points(node.has("points") ? node.get("points").asInt() : null)
                .data(data)
                .build();
    }
}
