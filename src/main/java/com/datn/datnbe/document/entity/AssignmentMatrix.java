package com.datn.datnbe.document.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentMatrix {

    String grade;
    String subject;
    String createdAt;
    Dimensions dimensions;
    List<List<List<String>>> matrix;
    Integer totalQuestions;
    Double totalPoints;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Dimensions {
        List<Topic> topics;
        List<String> difficulties;
        @JsonProperty("questionTypes")
        @JsonAlias("question_types")
        List<String> questionTypes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Topic {
        String id;
        String name;
        List<Subtopic> subtopics;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Subtopic {
        String id;
        String name;
    }
}
