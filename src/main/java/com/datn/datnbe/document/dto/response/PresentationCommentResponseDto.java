package com.datn.datnbe.document.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresentationCommentResponseDto {

    String id;
    String presentationId;
    String userId;
    String content;

    @Builder.Default
    List<String> mentionedUsers = new ArrayList<>();

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Enriched user data
    String userName;
    String userAvatar;
    Boolean isOwner;
}
