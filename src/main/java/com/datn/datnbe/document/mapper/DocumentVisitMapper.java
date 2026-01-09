package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.response.RecentDocumentDto;
import com.datn.datnbe.document.entity.DocumentVisit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentVisitMapper {

    @Mapping(target = "documentTitle", ignore = true)
    @Mapping(target = "thumbnail", ignore = true)
    RecentDocumentDto toRecentDocumentDto(DocumentVisit documentVisit);
}
