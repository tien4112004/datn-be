package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.request.PostCreateRequest;
import com.datn.datnbe.cms.dto.request.PostUpdateRequest;
import com.datn.datnbe.cms.dto.response.PostResponseDto;
import com.datn.datnbe.cms.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "isPinned", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toEntity(PostCreateRequest request);

    PostResponseDto toResponseDto(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PostUpdateRequest request, @org.mapstruct.MappingTarget Post entity);
}
