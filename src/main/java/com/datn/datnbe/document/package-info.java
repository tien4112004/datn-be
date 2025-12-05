@ApplicationModule(allowedDependencies = {"ai :: AiApi", "sharedkernel", "auth :: authApi", "auth :: AuthRequestDto",
        "auth :: AuthResponseDto", "sharedkernel::dto", "sharedkernel::exceptions"})
package com.datn.datnbe.document;

import org.springframework.modulith.ApplicationModule;
