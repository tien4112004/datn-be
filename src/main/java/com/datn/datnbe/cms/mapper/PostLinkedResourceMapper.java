package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.LinkedResourceDto;
import com.datn.datnbe.cms.entity.PostLinkedResource;
import com.datn.datnbe.cms.enums.LinkedResourceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostLinkedResourceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "resourceType", source = "type", qualifiedByName = "stringToResourceType")
    @Mapping(target = "resourceId", source = "id")
    PostLinkedResource toEntity(LinkedResourceDto dto);

    @Mapping(target = "type", source = "resourceType", qualifiedByName = "resourceTypeToString")
    @Mapping(target = "id", source = "resourceId")
    LinkedResourceDto toDto(PostLinkedResource entity);

    List<PostLinkedResource> toEntityList(List<LinkedResourceDto> dtos);

    List<LinkedResourceDto> toDtoList(List<PostLinkedResource> entities);

    @Named("stringToResourceType")
    default LinkedResourceType stringToResourceType(String type) {
        if (type == null) {
            return null;
        }
        return LinkedResourceType.fromValue(type);
    }

    @Named("resourceTypeToString")
    default String resourceTypeToString(LinkedResourceType resourceType) {
        if (resourceType == null) {
            return null;
        }
        return resourceType.getValue();
    }
}
