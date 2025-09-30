package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.SlidesUpsertRequest;

public interface SlidesApi {
    void upsertSlides(String id, SlidesUpsertRequest request);
}
