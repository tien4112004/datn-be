package com.datn.datnbe.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRegistrationResponse {

    private String fileId;

    private String fileName;

    private String keycloakResourceId;

    private String ownerId;

    private String message;

    private boolean success;
}
