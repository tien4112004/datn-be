package com.datn.datnbe.document.presentation;

import com.datn.datnbe.document.dto.response.ChapterResponse;
import com.datn.datnbe.document.management.ChapterManagement;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterPresentation {

    private final ChapterManagement chapterManagement;

    @GetMapping
    public ResponseEntity<AppResponseDto<List<ChapterResponse>>> getAllChapters(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String subject) {
        return ResponseEntity.ok(AppResponseDto.success(chapterManagement.getAllChapters(grade, subject)));
    }
}
