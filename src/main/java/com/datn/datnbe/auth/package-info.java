/**
 * Authentication and Authorization module.
 *
 * This module provides:
 * - User authentication and authorization via Keycloak
 * - JWT token management
 * - User profile management
 * - File sharing with Keycloak Authorization Services
 * - Resource-based access control
 */
@ApplicationModule(allowedDependencies = {"sharedkernel"})
package com.datn.datnbe.auth;

import org.springframework.modulith.ApplicationModule;
