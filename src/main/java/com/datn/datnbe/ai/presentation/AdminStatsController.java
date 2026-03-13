package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.dto.response.AdminStatsDto;
import com.datn.datnbe.ai.service.AdminStatsService;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatsController {

    AdminStatsService adminStatsService;

    @GetMapping
    public ResponseEntity<AppResponseDto<AdminStatsDto>> getStats() {
        log.info("Admin fetching dashboard stats");
        return ResponseEntity.ok(AppResponseDto.success(adminStatsService.getStats()));
    }
}
