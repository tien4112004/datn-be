package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.request.PresentationCommentCreateRequest;
import com.datn.datnbe.document.dto.request.PresentationCommentUpdateRequest;
import com.datn.datnbe.document.dto.response.PresentationCommentResponseDto;
import com.datn.datnbe.document.entity.PresentationComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring", imports = {
        Date.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PresentationCommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "presentationId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "content", source = "content")
    @Mapping(target = "mentionedUsers", source = "mentionedUsers")
    PresentationComment createRequestToEntity(PresentationCommentCreateRequest request);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "presentationId", target = "presentationId")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "mentionedUsers", target = "mentionedUsers")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "isOwner", ignore = true)
    PresentationCommentResponseDto entityToResponseDto(PresentationComment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "presentationId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "content", source = "content")
    @Mapping(target = "mentionedUsers", source = "mentionedUsers")
    void updateEntityFromRequest(PresentationCommentUpdateRequest request, @MappingTarget PresentationComment comment);

    List<PresentationCommentResponseDto> entitiesToResponseDtos(List<PresentationComment> comments);
}
