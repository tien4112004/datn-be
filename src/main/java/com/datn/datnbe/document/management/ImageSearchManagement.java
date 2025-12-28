package com.datn.datnbe.document.management;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.datn.datnbe.document.api.ImageSearchApi;
import com.datn.datnbe.document.apiclient.PexelsApiClient;
import com.datn.datnbe.document.dto.request.PexelsImageSearchRequest;
import com.datn.datnbe.document.dto.response.ImageSearchResultDto;
import com.datn.datnbe.document.dto.response.PexelsImageResponse;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginationDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageSearchManagement implements ImageSearchApi {
    private final PexelsApiClient pexelsApiClient;

    @Override
    public PaginatedResponseDto<ImageSearchResultDto> searchImages(PexelsImageSearchRequest request) {

        log.info("Searching images: query={}, orientation={}, page={}",
                request.getQuery(),
                request.getOrientation(),
                request.getPage());

        PexelsImageResponse response = pexelsApiClient
                .searchPhotos(request.getQuery(), request.getOrientation(), request.getPage(), request.getPerPage())
                .block();

        if (response == null || response.getPhotos() == null) {
            log.warn("No results from Pexels for query: {}", request.getQuery());
            return PaginatedResponseDto.<ImageSearchResultDto>builder()
                    .data(Collections.emptyList())
                    .pagination(PaginationDto.builder()
                            .currentPage(request.getPage())
                            .pageSize(request.getPerPage())
                            .totalItems(0L)
                            .totalPages(0)
                            .build())
                    .build();
        }

        List<ImageSearchResultDto> results = response.getPhotos().stream().map(this::mapToDto).toList();

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(request.getPage())
                .pageSize(request.getPerPage())
                .totalItems((long) results.size())
                .totalPages(response.getNextPage() != null ? request.getPage() + 1 : request.getPage())
                .build();

        return PaginatedResponseDto.<ImageSearchResultDto>builder().data(results).pagination(pagination).build();
    }

    private ImageSearchResultDto mapToDto(PexelsImageResponse.PexelsPhoto photo) {
        return ImageSearchResultDto.builder()
                .id(photo.getId())
                .width(photo.getWidth())
                .height(photo.getHeight())
                .src(photo.getSrc().getLarge())
                .photographer(photo.getPhotographer())
                .photographerUrl(photo.getPhotographerUrl())
                .alt(photo.getAlt())
                .build();
    }
}
