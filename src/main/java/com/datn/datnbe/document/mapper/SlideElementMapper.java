package com.datn.datnbe.document.mapper;

import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
@Named("SlideElementMapper")
public interface SlideElementMapper {
    // Simplified mapper - SlideElement now only has id and extraFields
    // No complex mappings needed
}
