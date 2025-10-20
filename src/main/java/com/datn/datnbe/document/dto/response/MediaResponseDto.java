package com.datn.datnbe.document.dto.response;

import com.datn.datnbe.sharedkernel.enums.MediaType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaResponseDto {
    Long id;
    String originalFilename;
    @JsonProperty(value = "url")
    String cdnUrl;
    MediaType mediaType;
    Long fileSize;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
