package com.datn.datnbe.cms.dto.request;

import com.datn.datnbe.cms.dto.AttachmentDto;
import com.datn.datnbe.cms.dto.LinkedResourceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class PostCreateRequest {
    @NotBlank
    private String content;

    private String type; // Post, Assignment

    private List<AttachmentDto> attachments;

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
    private Double passingScore;
    private Date availableFrom;
    private Date availableUntil;
    private String assignmentTitle;
    @Builder.Default
    private Boolean autoGrade = true;
}
