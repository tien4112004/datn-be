package com.datn.datnbe.document.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContextResponse {
    String id;
    String title;
    String content;
    String subject;
    String grade;
    String author;
    Boolean fromBook;
    String ownerId;
    Date createdAt;
    Date updatedAt;
}
