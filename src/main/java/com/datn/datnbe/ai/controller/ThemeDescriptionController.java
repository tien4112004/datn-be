package com.datn.datnbe.ai.controller;

import com.datn.datnbe.ai.dto.request.ThemeDescriptionRequest;
import com.datn.datnbe.ai.dto.response.ThemeDescriptionResponse;
import com.datn.datnbe.ai.service.ColorDescriptionService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@Slf4j
public class ThemeDescriptionController {
    private final ColorDescriptionService colorDescriptionService;

    @PostMapping("/generate-description")
    public ResponseEntity<AppResponseDto<ThemeDescriptionResponse>> generateDescription(
            @Valid @RequestBody ThemeDescriptionRequest request) {

        log.info("Generating theme description for colors: primary={}, background={}, text={}",
                request.getPrimaryColor(),
                request.getBackgroundColor(),
                request.getTextColor());

        String description = colorDescriptionService
                .buildThemeDescription(request.getPrimaryColor(), request.getBackgroundColor(), request.getTextColor());

        ThemeDescriptionResponse response = new ThemeDescriptionResponse(description);
        return ResponseEntity.ok(AppResponseDto.success(response));
    }
}
