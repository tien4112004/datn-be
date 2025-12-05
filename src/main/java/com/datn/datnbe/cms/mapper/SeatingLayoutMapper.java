package com.datn.datnbe.cms.mapper;

import com.datn.datnbe.cms.dto.request.SeatingLayoutRequest;
import com.datn.datnbe.cms.dto.response.SeatingLayoutResponseDto;
import com.datn.datnbe.cms.entity.LayoutConfig;
import com.datn.datnbe.cms.entity.SeatingLayout;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@Named("SeatingLayoutMapper")
public interface SeatingLayoutMapper {

    @Named("toLayoutResponseDto")
    default SeatingLayoutResponseDto toLayoutResponseDto(SeatingLayout seatingLayout) {
        if (seatingLayout == null || seatingLayout.getLayoutConfig() == null) {
            return null;
        }
        LayoutConfig config = seatingLayout.getLayoutConfig();
        SeatingLayoutResponseDto responseDto = new SeatingLayoutResponseDto();
        if (config.getData() != null) {
            config.getData().forEach(responseDto::setData);
        }
        return responseDto;
    }

    @Named("toLayoutConfig")
    default LayoutConfig toLayoutConfig(SeatingLayoutRequest requestDto) {
        if (requestDto == null) {
            return null;
        }

        LayoutConfig layoutConfig = new LayoutConfig();
        if (requestDto.getData() != null) {
            requestDto.getData().forEach(layoutConfig::setData);
        }
        return layoutConfig;
    }
}
