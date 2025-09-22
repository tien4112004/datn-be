package com.datn.datnbe.document.dto.request;

import com.datn.datnbe.document.dto.SlideDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentationCreateRequest {
    private String id;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Slides cannot be null")
    @Valid
    private List<SlideDto> slides;

    @JsonProperty("meta_data")
    private Object metaData;

    Boolean isParsed;
}
