/**
 * CMS (Class Management System) module for managing classes, students,
 * schedules, and lessons.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"sharedkernel", "sharedkernel::dto",
        "sharedkernel::exceptions", "sharedkernel::config", "auth::authApi", "auth::AuthResponseDto", "auth",
        "document::DocumentApi", "document::DocumentResponseDto", "student::StudentApi"})

package com.datn.datnbe.cms;
