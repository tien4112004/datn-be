package com.datn.datnbe.document.api;

import org.springframework.web.multipart.MultipartFile;

public interface ImageApi {
    String uploadImage(MultipartFile file);
}
