package com.datn.datnbe.document.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlideThemesByIdsRequest {

    @NotEmpty(message = "IDs list cannot be empty")
    @Size(max = 20, message = "Maximum 20 IDs allowed per request")
    private List<String> ids;
}
