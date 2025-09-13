package com.datn.datnbe.ai.mapper;

import com.datn.datnbe.ai.dto.response.AIResultResponseDto;
import com.datn.datnbe.ai.entity.AIResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AIResultMapper {
    AIResultResponseDto toResponseDto(AIResult aiResult);
}
