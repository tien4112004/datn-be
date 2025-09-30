package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.request.MindmapCollectionRequest;
import com.datn.datnbe.document.dto.request.MindmapCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateTitleRequest;
import com.datn.datnbe.document.dto.response.MindmapCreateResponseDto;
import com.datn.datnbe.document.dto.response.MindmapDto;
import com.datn.datnbe.document.dto.response.MindmapListResponseDto;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;

import java.util.List;

public interface MindmapApi {
    MindmapCreateResponseDto createMindmap(MindmapCreateRequest request);

    List<MindmapListResponseDto> getAllMindmaps();

    PaginatedResponseDto<MindmapListResponseDto> getAllMindmaps(MindmapCollectionRequest request);

    void updateMindmap(String id, MindmapUpdateRequest request);

    void updateTitleMindmap(String id, MindmapUpdateTitleRequest request);

    MindmapDto getMindmap(String id);
}
