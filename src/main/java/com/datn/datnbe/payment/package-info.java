@ApplicationModule(allowedDependencies = {"sharedkernel", "sharedkernel::dto", "sharedkernel::exceptions",
        "sharedkernel::config", "auth", "auth::AuthResponseDto"})
package com.datn.datnbe.payment;

import org.springframework.modulith.ApplicationModule;
