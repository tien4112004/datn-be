package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.api.TokenUsageApi;
import com.datn.datnbe.ai.dto.request.TokenUsageFilterRequest;
import com.datn.datnbe.ai.dto.response.TokenUsageStatsDto;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/token-usage")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminTokenUsageController {
    TokenUsageApi tokenUsageApi;

    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<AppResponseDto<TokenUsageStatsDto>> getTokenUsageStats(@PathVariable String userId,
            @Valid @ModelAttribute TokenUsageFilterRequest filterRequest) {
        log.info(
                "Admin fetching token usage stats for user: {} with filters - model: {}, provider: {}, requestType: {}",
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

    @GetMapping("/users/{userId}/by-model")
    public ResponseEntity<AppResponseDto<List<TokenUsageStatsDto>>> getTokenUsageByModel(@PathVariable String userId) {
        log.info("Admin fetching token usage by model for user: {}", userId);

        List<TokenUsageStatsDto> results = tokenUsageApi.getTokenUsageByModel(userId);

        return ResponseEntity.ok(AppResponseDto.success(results));
    }

    @GetMapping("/users/{userId}/by-request-type")
    public ResponseEntity<AppResponseDto<List<TokenUsageStatsDto>>> getTokenUsageByRequestType(
            @PathVariable String userId) {
        log.info("Admin fetching token usage by request type for user: {}", userId);

        List<TokenUsageStatsDto> results = tokenUsageApi.getTokenUsageByRequestType(userId);

        return ResponseEntity.ok(AppResponseDto.success(results));
    }
}
