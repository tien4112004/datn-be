package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MindmapCommentCreateRequest {

    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 5000, message = "Comment content must not exceed 5000 characters")
    String content;

    @Builder.Default
    List<String> mentionedUsers = new ArrayList<>();
}
