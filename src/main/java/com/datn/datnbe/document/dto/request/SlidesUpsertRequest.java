package com.datn.datnbe.document.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlidesUpsertRequest {
    @Valid
    @NotNull
    private List<SlideUpdateRequest> slides;
}
