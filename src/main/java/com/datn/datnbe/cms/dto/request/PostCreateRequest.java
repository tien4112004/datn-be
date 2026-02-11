package com.datn.datnbe.cms.dto.request;

import com.datn.datnbe.cms.dto.LinkedResourceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PostCreateRequest {
    @NotBlank
    private String content;

    private String type; // Post, Assignment

    private List<String> attachments;

    @Valid
    private List<LinkedResourceDto> linkedResources;

    private String linkedLessonId;

    private String assignmentId;

    private Date dueDate;

    private Boolean allowComments;

    // Assignment settings (only for Homework type posts)
    private Integer maxSubmissions;
    private Boolean allowRetake;
    private Boolean shuffleQuestions;
    private Boolean showCorrectAnswers;
    private Boolean showScoreImmediately;
    private Double passingScore;
    private Date availableFrom;
    private Date availableUntil;
}
