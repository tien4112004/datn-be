package com.datn.datnbe.ai.presentation;

import com.datn.datnbe.ai.entity.ExamplePrompt;
import com.datn.datnbe.ai.enums.ExamplePromptType;
import com.datn.datnbe.ai.management.ExamplePromptManagement;
import com.datn.datnbe.sharedkernel.dto.AppResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/example-prompts")
@RequiredArgsConstructor
public class ExamplePromptController {

    private final ExamplePromptManagement examplePromptManagement;

    @GetMapping
    public ResponseEntity<AppResponseDto<List<ExamplePrompt>>> getExamplePrompts(@RequestParam ExamplePromptType type,
            @RequestParam(required = false, defaultValue = "vi") String language,
            @RequestParam(required = false, defaultValue = "5") int count) {
        return ResponseEntity
                .ok(AppResponseDto.success(examplePromptManagement.getExamplePrompts(type, language, count)));
    }
}
