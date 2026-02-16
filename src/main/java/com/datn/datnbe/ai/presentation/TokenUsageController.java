package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.request.TokenUsageFilterRequest;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/token-usage")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class TokenUsageController {
    TokenUsageApi tokenUsageApi;
    SecurityContextUtils securityContextUtils;

    @GetMapping("/stats")
    public ResponseEntity<AppResponseDto<TokenUsageStatsDto>> getTokenUsageStats(
            @Valid @ModelAttribute TokenUsageFilterRequest filterRequest) {
        String userId = securityContextUtils.getCurrentUserProfileId();
        log.info("Fetching token usage stats for user: {} with filters - model: {}, provider: {}, requestType: {}",
                userId,
                filterRequest.getModel(),
                filterRequest.getProvider(),
                filterRequest.getRequestType());

        TokenUsageStatsDto stats = tokenUsageApi.getStatsWithFilters(userId,
                filterRequest.getModel(),
                filterRequest.getProvider(),
                filterRequest.getRequestType());

        return ResponseEntity.ok(AppResponseDto.success(stats));
    }

    @GetMapping("/requests/image/count")
    public ResponseEntity<AppResponseDto<Long>> getImageRequestCount() {
        String userId = securityContextUtils.getCurrentUserProfileId();
        log.info("Fetching image generation request count for user: {}", userId);

        TokenUsageStatsDto stats = tokenUsageApi.getStatsWithFilters(userId, null, null, "image");
        Long imageRequestCount = stats.getTotalRequests();

        return ResponseEntity.ok(AppResponseDto.success(imageRequestCount));
    }

    @GetMapping("/by-model")
    public ResponseEntity<AppResponseDto<List<TokenUsageStatsDto>>> getTokenUsageByModel() {
        String userId = securityContextUtils.getCurrentUserProfileId();
        log.info("Fetching token usage by model for user: {}", userId);

        List<TokenUsageStatsDto> results = tokenUsageApi.getTokenUsageByModel(userId);

        return ResponseEntity.ok(AppResponseDto.success(results));
    }

    @GetMapping("/by-request-type")
    public ResponseEntity<AppResponseDto<List<TokenUsageStatsDto>>> getTokenUsageByRequestType() {
        String userId = securityContextUtils.getCurrentUserProfileId();
        log.info("Fetching token usage by request type for user: {}", userId);

        List<TokenUsageStatsDto> results = tokenUsageApi.getTokenUsageByRequestType(userId);

        return ResponseEntity.ok(AppResponseDto.success(results));
    }
}
