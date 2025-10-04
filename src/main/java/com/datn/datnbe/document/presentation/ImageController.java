package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.management.ImageManagement;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ImageController {
    ImageManagement imageManagement;

    @GetMapping
    public ResponseEntity<AppResponseDto> getImages(Pageable pageable) {
        var response = imageManagement.getImages(pageable);

        return ResponseEntity.ok(AppResponseDto.successWithPagination(response.getData(), response.getPagination()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppResponseDto> getImageById(@PathVariable Long id) {
        var response = imageManagement.getImageById(id);

        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
