package com.datn.datnbe.ai.management;

import com.datn.datnbe.sharedkernel.idempotency.api.AbstractIdempotencyService;
import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyKey;
import org.springframework.stereotype.Component;

@Component
public class ImageGenerationIdempotencyService extends AbstractIdempotencyService {
    @Override
    public IdempotencyKey initialize(String key) {
        return super.initialize(key + "-image");
    }

    @Override
    public boolean isValid(String key) {
        return super.isValid(key) && key.matches("^[a-fA-F0-9-]+:[a-fA-F0-9-]+:[a-zA-Z0-9-]+$");
    }
}
