package com.datn.datnbe.auth.dto.response;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePermissionResponse {

    private String resourceId;

    private String userId;

    private Set<String> permissions;

    private boolean hasAccess;
}
