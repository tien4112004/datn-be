package com.datn.datnbe.ai.management;

import com.datn.datnbe.ai.api.AIResultApi;
import com.datn.datnbe.ai.dto.response.AIResultResponseDto;
import com.datn.datnbe.ai.entity.AIResult;
import com.datn.datnbe.ai.mapper.AIResultMapper;
import com.datn.datnbe.ai.repository.AIResultRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AIResultManagement implements AIResultApi {
    AIResultRepository aiResultRepo;
    AIResultMapper aiResultMapper;

    @Override
    public AIResultResponseDto saveAIResult(String aiResult, String presentationId, String generationOptions) {
        log.info("Saving AI result for presentation {} with generation options: {}", presentationId, generationOptions);

        AIResult aiResultEntity = AIResult.builder()
                .result(aiResult)
                .presentationId(presentationId)
                .generationOptions(generationOptions)
                .build();

        AIResult savedEntity = aiResultRepo.save(aiResultEntity);

        return aiResultMapper.toResponseDto(savedEntity);
    }

    @Override
    public AIResultResponseDto getAIResultByPresentationId(String presentationId) {
        Optional<AIResult> aiResultOpt = aiResultRepo.findByPresentationId(presentationId);
        if (aiResultOpt.isEmpty()) {
            throw new AppException(ErrorCode.AI_RESULT_NOT_FOUND,
                    "AI Result with presentation ID " + presentationId + " not found");
        }
        AIResult aiResult = aiResultOpt.get();

        return AIResultResponseDto.builder()
                .id(aiResult.getId())
                .result(aiResult.getResult())
                .createdAt(aiResult.getCreatedAt().toString())
                .presentationId(aiResult.getPresentationId())
                .generationOptions(aiResult.getGenerationOptions())
                .build();
    }
}
