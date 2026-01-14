package com.datn.datnbe.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Combined response for ShareMenu initialization
 * Returns shared users, public access settings, and current user permission in a single call
 * Optimized to reduce network round-trips from 2 to 1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShareStateResponse {

    /**
     * List of users who have been granted access to this resource
     */
    List<SharedUserResponse> sharedUsers;

    /**
     * Public access configuration for the resource
     */
    PublicAccessResponse publicAccess;

    /**
     * Current user's permission level on this resource
     * Possible values: "read", "comment", "edit"
     */
    String currentUserPermission;
}
