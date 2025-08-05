package com.datn.document.mapper;

import com.datn.document.dto.SlideBackgroundDto;
import com.datn.document.dto.SlideDto;
import com.datn.document.dto.SlideElementDto;
import com.datn.document.dto.request.PresentationCreateRequest;
import com.datn.document.dto.response.PresentationCreateResponseDto;
import com.datn.document.entity.Presentation;
import com.datn.document.entity.valueobject.Slide;
import com.datn.document.entity.valueobject.SlideBackground;
import com.datn.document.entity.valueobject.SlideElement;
import com.datn.document.enums.SlideElementType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PresentationEntityMapper {

    public Presentation toEntity(PresentationCreateRequest request) {
        return Presentation.builder()
                .title(request.getTitle() != null ? request.getTitle() : "Untitled Presentation")
                .slides(mapSlidesToEntity(request.getSlides()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public PresentationCreateResponseDto toResponseDto(Presentation entity) {
        return PresentationCreateResponseDto.builder()
                .title(entity.getTitle())
                .presentation(mapSlidesToDto(entity.getSlides()))
                .build();
    }

    private List<Slide> mapSlidesToEntity(List<SlideDto> slideDtos) {
        if (slideDtos == null) {
            return null;
        }
        return slideDtos.stream()
                .map(this::mapSlideToEntity)
                .collect(Collectors.toList());
    }

    private List<SlideDto> mapSlidesToDto(List<Slide> slides) {
        if (slides == null) {
            return null;
        }
        return slides.stream()
                .map(this::mapSlideToDto)
                .collect(Collectors.toList());
    }

    private Slide mapSlideToEntity(SlideDto slideDto) {
        if (slideDto == null) {
            return null;
        }
        return Slide.builder()
                .id(slideDto.getId())
                .elements(mapElementsToEntity(slideDto.getElements()))
                .background(mapBackgroundToEntity(slideDto.getBackground()))
                .build();
    }

    private SlideDto mapSlideToDto(Slide slide) {
        if (slide == null) {
            return null;
        }
        return SlideDto.builder()
                .id(slide.getId())
                .elements(mapElementsToDto(slide.getElements()))
                .background(mapBackgroundToDto(slide.getBackground()))
                .build();
    }

    private List<SlideElement> mapElementsToEntity(List<SlideElementDto> elementDtos) {
        if (elementDtos == null) {
            return null;
        }
        return elementDtos.stream()
                .map(this::mapElementToEntity)
                .collect(Collectors.toList());
    }

    private List<SlideElementDto> mapElementsToDto(List<SlideElement> elements) {
        if (elements == null) {
            return null;
        }
        return elements.stream()
                .map(this::mapElementToDto)
                .collect(Collectors.toList());
    }

    private SlideElement mapElementToEntity(SlideElementDto elementDto) {
        if (elementDto == null) {
            return null;
        }
        return SlideElement.builder()
                .type(elementDto.getType() != null ? elementDto.getType().getValue() : null)
                .id(elementDto.getId())
                .left(elementDto.getLeft())
                .top(elementDto.getTop())
                .width(elementDto.getWidth())
                .height(elementDto.getHeight())
                .viewBox(elementDto.getViewBox())
                .path(elementDto.getPath())
                .fill(elementDto.getFill())
                .fixedRatio(elementDto.getFixedRatio())
                .opacity(elementDto.getOpacity())
                .rotate(elementDto.getRotate())
                .flipV(elementDto.getFlipV())
                .lineHeight(elementDto.getLineHeight())
                .content(elementDto.getContent())
                .defaultFontName(elementDto.getDefaultFontName())
                .defaultColor(elementDto.getDefaultColor())
                .start(elementDto.getStart())
                .end(elementDto.getEnd())
                .points(elementDto.getPoints())
                .color(elementDto.getColor())
                .style(elementDto.getStyle())
                .wordSpace(elementDto.getWordSpace())
                .build();
    }

    private SlideElementDto mapElementToDto(SlideElement element) {
        if (element == null) {
            return null;
        }
        return SlideElementDto.builder()
                .type(convertStringToSlideElementType(element.getType()))
                .id(element.getId())
                .left(element.getLeft())
                .top(element.getTop())
                .width(element.getWidth())
                .height(element.getHeight())
                .viewBox(element.getViewBox())
                .path(element.getPath())
                .fill(element.getFill())
                .fixedRatio(element.getFixedRatio())
                .opacity(element.getOpacity())
                .rotate(element.getRotate())
                .flipV(element.getFlipV())
                .lineHeight(element.getLineHeight())
                .content(element.getContent())
                .defaultFontName(element.getDefaultFontName())
                .defaultColor(element.getDefaultColor())
                .start(element.getStart())
                .end(element.getEnd())
                .points(element.getPoints())
                .color(element.getColor())
                .style(element.getStyle())
                .wordSpace(element.getWordSpace())
                .build();
    }

    private SlideBackground mapBackgroundToEntity(SlideBackgroundDto backgroundDto) {
        if (backgroundDto == null) {
            return null;
        }
        return SlideBackground.builder()
                .type(backgroundDto.getType())
                .color(backgroundDto.getColor())
                .build();
    }

    private SlideBackgroundDto mapBackgroundToDto(SlideBackground background) {
        if (background == null) {
            return null;
        }
        return SlideBackgroundDto.builder()
                .type(background.getType())
                .color(background.getColor())
                .build();
    }
    
    private SlideElementType convertStringToSlideElementType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return SlideElementType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Return null or default value for invalid types
            return null;
        }
    }
}