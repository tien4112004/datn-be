package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.SlidesUpsertRequest;
import org.springframework.stereotype.Service;

@Service
public interface SlidesApi {
    void upsertSlides(String id, SlidesUpsertRequest request);
}
