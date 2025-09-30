package com.datn.datnbe.document.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MindmapUpdateTitleAndDescriptionRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description;

    public boolean isValid() {
        return (title != null && !title.isBlank()) || (description != null && !description.isBlank());
    }

}
