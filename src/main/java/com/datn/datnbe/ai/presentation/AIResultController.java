package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.AIResultApi;
import com.datn.datnbe.ai.dto.response.AIResultResponseDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AIResultController {
    AIResultApi aiResultApi;

    @GetMapping("/presentations/{presentationId}/ai-result")
    public ResponseEntity<AppResponseDto<AIResultResponseDto>> getAIResultByPresentationId(
            @PathVariable String presentationId) {
        AIResultResponseDto aiResultResponseDto = aiResultApi.getAIResultByPresentationId(presentationId);
        AppResponseDto<AIResultResponseDto> responseDto = AppResponseDto.<AIResultResponseDto>builder()
                .data(aiResultResponseDto)
                .build();
        return ResponseEntity.ok(responseDto);
    }
}
