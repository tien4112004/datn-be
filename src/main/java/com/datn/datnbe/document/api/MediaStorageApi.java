package com.datn.datnbe.document.api;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageApi {
    String upload(MultipartFile file);
}
