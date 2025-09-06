package com.datn.datnbe.sharedkernel.idempotency.internal;

import com.datn.datnbe.sharedkernel.idempotency.api.AbstractIdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultIdempotencyService extends AbstractIdempotencyService {
    // Inherits all default behavior from AbstractIdempotencyService
}
