package com.datn.datnbe.ai.management;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
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
        return super.isValid(key) && key.matches("^[a-fA-F0-9-]+:[a-fA-F0-9-]+$");
    }

    @Override
    public void invalidate(String key) {
        throw new AppException(ErrorCode.IDEMPOTENCY_KEY_INVALID,
                "Valid keys must be in the format '{presentationId}:{slideId}'");
    }

    @Override
    public String getActualKey(String originalKey) {
        return originalKey + "-image";
    }
}
