package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContextsByIdsRequest {

    @NotEmpty(message = "IDs list cannot be empty")
    @Size(max = 50, message = "Maximum 50 IDs allowed per request")
    List<String> ids;
}
