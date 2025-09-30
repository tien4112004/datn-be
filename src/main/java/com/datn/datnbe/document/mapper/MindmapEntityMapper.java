package com.datn.datnbe.document.mapper;

import com.datn.datnbe.document.dto.MindmapNodeDto;
import com.datn.datnbe.document.dto.MindmapEdgeDto;
import com.datn.datnbe.document.dto.request.MindmapCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateRequest;
import com.datn.datnbe.document.dto.response.MindmapDto;
import com.datn.datnbe.document.dto.response.MindmapListResponseDto;
import com.datn.datnbe.document.entity.Mindmap;
import com.datn.datnbe.document.entity.valueobject.MindmapNode;
import com.datn.datnbe.document.entity.valueobject.MindmapEdge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class,
        UUID.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MindmapEntityMapper {

    @Mapping(target = "title", expression = "java((request.getTitle() == null || request.getTitle().isEmpty()) ? \"Untitled Presentation\" : request.getTitle())")
    @Mapping(target = "description", expression = "java(request.getDescription())")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "nodes", source = "nodes")
    @Mapping(target = "edges", source = "edges")
    Mindmap createRequestToEntity(MindmapCreateRequest request);

    @Mapping(source = "nodes", target = "nodes")
    @Mapping(source = "edges", target = "edges")
    MindmapDto entityToDto(Mindmap mindmap);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    MindmapListResponseDto entityToListResponse(Mindmap mindmap);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "nodes", source = "nodes")
    @Mapping(target = "edges", source = "edges")
    void updateEntityFromRequest(MindmapUpdateRequest request, @MappingTarget Mindmap mindmap);

    @Mapping(target = "id", expression = "java(nodeDto.getId() != null ? nodeDto.getId() : UUID.randomUUID().toString())")
    @Mapping(target = "extraFields", source = "extraFields")
    MindmapNode nodeDtoToEntity(MindmapNodeDto nodeDto);

    @Mapping(source = "extraFields", target = "extraFields")
    MindmapNodeDto nodeEntityToDto(MindmapNode node);

    @Mapping(target = "id", expression = "java(edgeDto.getId() != null ? edgeDto.getId() : UUID.randomUUID().toString())")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "extraFields", source = "extraFields")
    MindmapEdge edgeDtoToEntity(MindmapEdgeDto edgeDto);

    @Mapping(source = "type", target = "type")
    @Mapping(source = "extraFields", target = "extraFields")
    MindmapEdgeDto edgeEntityToDto(MindmapEdge edge);

    List<MindmapNode> nodeDtosToEntities(List<MindmapNodeDto> nodeDtos);

    List<MindmapNodeDto> nodeEntitiesToDtos(List<MindmapNode> nodes);

    List<MindmapEdge> edgeDtosToEntities(List<MindmapEdgeDto> edgeDtos);

    List<MindmapEdgeDto> edgeEntitiesToDtos(List<MindmapEdge> edges);

}
