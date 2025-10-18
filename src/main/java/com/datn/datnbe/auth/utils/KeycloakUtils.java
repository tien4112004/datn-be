package com.datn.datnbe.auth.utils;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;

public class KeycloakUtils {

    public static String extractUserIdFromLocation(String locationHeader) {
        if (locationHeader == null) {
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR, "Could not extract user ID from Keycloak response");
        }
        String[] parts = locationHeader.split("/");
        return parts[parts.length - 1];
    }
}
