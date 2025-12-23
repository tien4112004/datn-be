package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.response.CommentResponseDto;
import com.datn.datnbe.cms.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommentMapper {
    CommentResponseDto toResponseDto(Comment comment);
}
