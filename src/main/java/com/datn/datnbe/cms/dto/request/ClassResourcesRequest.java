package com.datn.datnbe.cms.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClassResourcesRequest {

    @Size(max = 100, message = "Search query cannot exceed 100 characters")
    private String search;

    @Pattern(regexp = "^(mindmap|presentation|assignment)?$", message = "Type must be mindmap, presentation, or assignment")
    private String type;
}
