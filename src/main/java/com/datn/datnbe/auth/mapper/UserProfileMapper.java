package com.datn.datnbe.auth.mapper;

import com.datn.datnbe.auth.dto.request.SignupRequest;
import com.datn.datnbe.auth.dto.request.UserProfileUpdateRequest;
import com.datn.datnbe.auth.dto.response.UserProfileResponse;
import com.datn.datnbe.auth.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfileResponse toResponseDto(UserProfile userProfile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserProfile toEntity(SignupRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UserProfileUpdateRequest request, @MappingTarget UserProfile userProfile);
}
