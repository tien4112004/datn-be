package com.datn.datnbe.document.api;

import org.springframework.web.multipart.MultipartFile;

import com.datn.datnbe.document.dto.request.MindmapCollectionRequest;
import com.datn.datnbe.document.dto.request.MindmapCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateTitleAndDescriptionRequest;
import com.datn.datnbe.document.dto.response.MindmapCreateResponseDto;
import com.datn.datnbe.document.dto.response.MindmapDto;
import com.datn.datnbe.document.dto.response.MindmapListResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

public interface MindmapApi {
    MindmapCreateResponseDto createMindmap(MindmapCreateRequest request);

    PaginatedResponseDto<MindmapListResponseDto> getAllMindmaps(MindmapCollectionRequest request);

    void updateMindmap(String id, MindmapUpdateRequest request, MultipartFile thumbnailFile);

    void updateTitleAndDescriptionMindmap(String id, MindmapUpdateTitleAndDescriptionRequest request);

    MindmapDto getMindmap(String id);
}
