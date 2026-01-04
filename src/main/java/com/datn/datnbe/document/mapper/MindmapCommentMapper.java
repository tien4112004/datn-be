package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.MindmapCommentCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapCommentUpdateRequest;
import com.datn.datnbe.document.dto.response.MindmapCommentResponseDto;
import com.datn.datnbe.document.entity.MindmapComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {
        LocalDateTime.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MindmapCommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mindmapId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "content", source = "content")
    @Mapping(target = "mentionedUsers", source = "mentionedUsers")
    MindmapComment createRequestToEntity(MindmapCommentCreateRequest request);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "mindmapId", target = "mindmapId")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "mentionedUsers", target = "mentionedUsers")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "isOwner", ignore = true)
    MindmapCommentResponseDto entityToResponseDto(MindmapComment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mindmapId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "content", source = "content")
    @Mapping(target = "mentionedUsers", source = "mentionedUsers")
    void updateEntityFromRequest(MindmapCommentUpdateRequest request, @MappingTarget MindmapComment comment);

    List<MindmapCommentResponseDto> entitiesToResponseDtos(List<MindmapComment> comments);
}
