package com.datn.datnbe.auth.dto.response;

import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for file sharing operations.
 * Contains information about the share operation result.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileShareResponse {

    /**
     * The ID of the file that was shared
     */
    String fileId;

    /**
     * The name of the file
     */
    String fileName;

    /**
     * The user ID of the user the file was shared with
     */
    String sharedWithUserId;

    /**
     * The username of the user the file was shared with
     */
    String sharedWithUsername;

    /**
     * The permissions that were granted
     */
    Set<String> grantedPermissions;

    /**
     * Message describing the result of the operation
     */
    String message;

    /**
     * Whether the operation was successful
     */
    @Builder.Default
    Boolean success = true;
}
