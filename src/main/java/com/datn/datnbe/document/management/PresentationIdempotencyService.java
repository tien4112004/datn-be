package com.datn.datnbe.document.management;

import com.datn.datnbe.sharedkernel.idempotency.api.IdempotencyKey;
import com.datn.datnbe.sharedkernel.idempotency.api.AbstractIdempotencyService;
import org.springframework.stereotype.Component;

@Component
public class PresentationIdempotencyService extends AbstractIdempotencyService {
    @Override
    public IdempotencyKey initialize(String key) {
        return super.initialize(key + ":update");
    }

    @Override
    public boolean isCompleted(IdempotencyKey entity) {
        return false;
    }

    @Override
    public boolean isValid(String key) {
        return super.isValid(key) && key.matches("^[a-fA-F0-9-]+:[a-fA-F0-9-]+$");
    }
}
