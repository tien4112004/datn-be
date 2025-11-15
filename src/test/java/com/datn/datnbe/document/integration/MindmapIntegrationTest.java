package com.datn.datnbe.document.integration;

import com.datn.datnbe.document.dto.MindmapNodeDto;
import com.datn.datnbe.document.dto.MindmapEdgeDto;
import com.datn.datnbe.document.dto.request.MindmapCollectionRequest;
import com.datn.datnbe.document.dto.request.MindmapCreateRequest;
import com.datn.datnbe.document.dto.request.MindmapUpdateTitleAndDescriptionRequest;
import com.datn.datnbe.document.dto.response.MindmapCreateResponseDto;
import com.datn.datnbe.document.dto.response.MindmapDto;
import com.datn.datnbe.document.dto.response.MindmapListResponseDto;
import com.datn.datnbe.document.entity.Mindmap;
import com.datn.datnbe.document.entity.valueobject.MindmapEdge;
import com.datn.datnbe.document.entity.valueobject.MindmapNode;
import com.datn.datnbe.document.management.MindmapManagement;
import com.datn.datnbe.document.repository.MindmapRepository;
import com.datn.datnbe.sharedkernel.dto.PaginatedResponseDto;
import com.datn.datnbe.sharedkernel.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
public class MindmapIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.8");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    MindmapManagement management;

    @Autowired
    MindmapRepository repository;

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    // ========== CREATE TESTS ==========

    @Test
    void createMindmap_persistsToDatabase() {
        // Arrange
        MindmapCreateRequest request = MindmapCreateRequest.builder()
                .title("Test Mindmap")
                .description("Test Description")
                .nodes(List.of())
                .edges(List.of())
                .build();

        // Act
        MindmapCreateResponseDto response = management.createMindmap(request);

        // Assert - Verify it's actually in the database
        Optional<Mindmap> savedMindmap = repository.findById(response.getId());
        assertThat(savedMindmap).isPresent();
        assertThat(savedMindmap.get().getTitle()).isEqualTo("Test Mindmap");
        assertThat(savedMindmap.get().getDescription()).isEqualTo("Test Description");
    }

    @Test
    void createMindmap_withNodesAndEdges_persistsAllData() {
        // Arrange
        MindmapNodeDto node = MindmapNodeDto.builder()
                .type("testNode")
                .extraFields(Map.of("key", "value", "number", 42))
                .build();

        MindmapEdgeDto edge = MindmapEdgeDto.builder()
                .type("testEdge")
                .extraFields(Map.of("source", "n1", "target", "n2"))
                .build();

        MindmapCreateRequest request = MindmapCreateRequest.builder()
                .title("Complex Mindmap")
                .description("With nodes and edges")
                .nodes(List.of(node))
                .edges(List.of(edge))
                .build();

        // Act
        MindmapCreateResponseDto response = management.createMindmap(request);

        // Assert - Query database directly
        Optional<Mindmap> savedMindmap = repository.findById(response.getId());
        assertThat(savedMindmap).isPresent();

        Mindmap mindmap = savedMindmap.get();
        assertThat(mindmap.getNodes()).hasSize(1);
        assertThat(mindmap.getNodes().get(0).getId()).isNotNull();
        assertThat(mindmap.getNodes().get(0).getType()).isEqualTo("testNode");
        assertThat(mindmap.getNodes().get(0).getExtraFields()).containsEntry("key", "value");
        assertThat(mindmap.getNodes().get(0).getExtraFields()).containsEntry("number", 42);

        assertThat(mindmap.getEdges()).hasSize(1);
        assertThat(mindmap.getEdges().get(0).getId()).isNotNull();
        assertThat(mindmap.getEdges().get(0).getType()).isEqualTo("testEdge");
        assertThat(mindmap.getEdges().get(0).getExtraFields()).containsEntry("source", "n1");
    }

    @Test
    void createMindmap_assignsUniqueIdsToNodes() {
        // Arrange
        MindmapCreateRequest request = MindmapCreateRequest.builder()
                .title("Multi Node Test")
                .description("Test")
                .nodes(List.of(MindmapNodeDto.builder().type("node1").extraFields(Map.of()).build(),
                        MindmapNodeDto.builder().type("node2").extraFields(Map.of()).build(),
                        MindmapNodeDto.builder().type("node3").extraFields(Map.of()).build()))
                .edges(List.of())
                .build();

        // Act
        MindmapCreateResponseDto response = management.createMindmap(request);

        // Assert - Verify unique IDs in database
        Mindmap saved = repository.findById(response.getId()).orElseThrow();
        assertThat(saved.getNodes()).hasSize(3);

        List<String> nodeIds = saved.getNodes().stream().map(MindmapNode::getId).toList();

        assertThat(nodeIds).doesNotHaveDuplicates();
        assertThat(nodeIds).allMatch(id -> id != null && !id.isEmpty());
    }

    @Test
    void createMindmap_withComplexExtraFields_persistsAllDataTypes() {
        // Arrange
        Map<String, Object> complexFields = Map
                .of("string", "text", "number", 42, "decimal", 3.14, "boolean", true, "list", List.of("a", "b", "c"));

        MindmapNodeDto node = MindmapNodeDto.builder().type("complex").extraFields(complexFields).build();

        MindmapCreateRequest request = MindmapCreateRequest.builder()
                .title("Complex Types")
                .description("Testing data types")
                .nodes(List.of(node))
                .edges(List.of())
                .build();

        // Act
        String id = management.createMindmap(request).getId();

        // Assert - Verify all data types persisted correctly in database
        Mindmap saved = repository.findById(id).orElseThrow();
        Map<String, Object> savedFields = saved.getNodes().get(0).getExtraFields();

        assertThat(savedFields.get("string")).isEqualTo("text");
        assertThat(savedFields.get("number")).isEqualTo(42);
        assertThat(savedFields.get("decimal")).isEqualTo(3.14);
        assertThat(savedFields.get("boolean")).isEqualTo(true);
        assertThat(savedFields.get("list")).isInstanceOf(List.class);
    }

    @Test
    void createMindmap_setsTimestampsInDatabase() {
        // Arrange
        MindmapCreateRequest request = MindmapCreateRequest.builder()
                .title("Timestamp Test")
                .description("Testing timestamps")
                .nodes(List.of())
                .edges(List.of())
                .build();

        // Act
        String id = management.createMindmap(request).getId();

        // Assert - Verify timestamps in database
        Mindmap saved = repository.findById(id).orElseThrow();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(saved.getUpdatedAt());
    }

    @Test
    void createMultipleMindmaps_allPersistedWithUniqueIds() {
        // Arrange & Act
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MindmapCreateRequest request = MindmapCreateRequest.builder()
                    .title("Mindmap " + i)
                    .description("Description " + i)
                    .nodes(List.of())
                    .edges(List.of())
                    .build();
            ids.add(management.createMindmap(request).getId());
        }

        // Assert - All IDs are unique
        assertThat(ids).doesNotHaveDuplicates();

        // Verify all are in database
        for (String id : ids) {
            assertThat(repository.findById(id)).isPresent();
        }

        assertThat(repository.count()).isEqualTo(10);
    }

    // ========== UPDATE TESTS ==========

    @Test
    void updateTitleAndDescription_persistsChangesToDatabase() {
        // Arrange - Create a mindmap first
        MindmapCreateRequest createRequest = MindmapCreateRequest.builder()
                .title("Original Title")
                .description("Original Description")
                .nodes(List.of())
                .edges(List.of())
                .build();
        String id = management.createMindmap(createRequest).getId();

        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .build();

        // Act
        management.updateTitleAndDescriptionMindmap(id, updateRequest);

        // Assert - Verify changes in database
        Mindmap updated = repository.findById(id).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void updateTitleAndDescription_onlyTitle_persistsPartialUpdate() {
        // Arrange
        MindmapCreateRequest createRequest = MindmapCreateRequest.builder()
                .title("Original Title")
                .description("Original Description")
                .nodes(List.of())
                .edges(List.of())
                .build();
        String id = management.createMindmap(createRequest).getId();

        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .title("New Title Only")
                .build();

        // Act
        management.updateTitleAndDescriptionMindmap(id, updateRequest);

        // Assert - Only title should be updated in database
        Mindmap updated = repository.findById(id).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("New Title Only");
        assertThat(updated.getDescription()).isEqualTo("Original Description");
    }

    @Test
    void updateTitleAndDescription_onlyDescription_persistsPartialUpdate() {
        // Arrange
        MindmapCreateRequest createRequest = MindmapCreateRequest.builder()
                .title("Original Title")
                .description("Original Description")
                .nodes(List.of())
                .edges(List.of())
                .build();
        String id = management.createMindmap(createRequest).getId();

        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .description("New Description Only")
                .build();

        // Act
        management.updateTitleAndDescriptionMindmap(id, updateRequest);

        // Assert - Only description should be updated in database
        Mindmap updated = repository.findById(id).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Original Title");
        assertThat(updated.getDescription()).isEqualTo("New Description Only");
    }

    @Test
    void updateTitleAndDescription_doesNotModifyNodesAndEdges() {
        // Arrange
        MindmapNodeDto node = MindmapNodeDto.builder().type("original").extraFields(Map.of("data", "value")).build();

        MindmapCreateRequest createRequest = MindmapCreateRequest.builder()
                .title("Original")
                .description("Original")
                .nodes(List.of(node))
                .edges(List.of())
                .build();
        String id = management.createMindmap(createRequest).getId();

        Mindmap beforeUpdate = repository.findById(id).orElseThrow();
        String originalNodeId = beforeUpdate.getNodes().get(0).getId();

        // Act
        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .title("Updated Title")
                .build();
        management.updateTitleAndDescriptionMindmap(id, updateRequest);

        // Assert - Nodes should remain unchanged in database
        Mindmap afterUpdate = repository.findById(id).orElseThrow();
        assertThat(afterUpdate.getNodes()).hasSize(1);
        assertThat(afterUpdate.getNodes().get(0).getId()).isEqualTo(originalNodeId);
        assertThat(afterUpdate.getNodes().get(0).getType()).isEqualTo("original");
        assertThat(afterUpdate.getNodes().get(0).getExtraFields()).containsEntry("data", "value");
    }

    @Test
    void updateTitleAndDescription_updatesTimestampInDatabase() throws InterruptedException {
        // Arrange
        MindmapCreateRequest createRequest = MindmapCreateRequest.builder()
                .title("Original")
                .description("Original")
                .nodes(List.of())
                .edges(List.of())
                .build();
        String id = management.createMindmap(createRequest).getId();

        Mindmap original = repository.findById(id).orElseThrow();
        Thread.sleep(100);

        // Act
        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .title("Updated")
                .build();
        management.updateTitleAndDescriptionMindmap(id, updateRequest);

        // Assert - Verify timestamp updated in database
        Mindmap updated = repository.findById(id).orElseThrow();
        assertThat(updated.getUpdatedAt()).isAfter(original.getUpdatedAt());
        assertThat(updated.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }

    @Test
    void updateTitleAndDescription_withNullValues_doesNotChangeDatabaseValues() {
        // Arrange
        MindmapCreateRequest createRequest = MindmapCreateRequest.builder()
                .title("Original Title")
                .description("Original Description")
                .nodes(List.of())
                .edges(List.of())
                .build();
        String id = management.createMindmap(createRequest).getId();

        // Act
        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .title(null)
                .description(null)
                .build();
        management.updateTitleAndDescriptionMindmap(id, updateRequest);

        // Assert - Database values should remain unchanged
        Mindmap unchanged = repository.findById(id).orElseThrow();
        assertThat(unchanged.getTitle()).isEqualTo("Original Title");
        assertThat(unchanged.getDescription()).isEqualTo("Original Description");
    }

    @Test
    void updateTitleAndDescription_withEmptyStrings_doesNotChangeDatabaseValues() {
        // Arrange
        MindmapCreateRequest createRequest = MindmapCreateRequest.builder()
                .title("Original Title")
                .description("Original Description")
                .nodes(List.of())
                .edges(List.of())
                .build();
        String id = management.createMindmap(createRequest).getId();

        // Act
        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .title("")
                .description("   ")
                .build();
        management.updateTitleAndDescriptionMindmap(id, updateRequest);

        // Assert - Database values should remain unchanged
        Mindmap unchanged = repository.findById(id).orElseThrow();
        assertThat(unchanged.getTitle()).isEqualTo("Original Title");
        assertThat(unchanged.getDescription()).isEqualTo("Original Description");
    }

    @Test
    void updateTitleAndDescription_whenNotInDatabase_throwsResourceNotFoundException() {
        // Arrange
        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .title("New Title")
                .build();

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> management.updateTitleAndDescriptionMindmap("non-existent-id", updateRequest));
    }

    @Test
    void updateTitleAndDescription_preservesIdInDatabase() {
        // Arrange
        MindmapCreateRequest createRequest = MindmapCreateRequest.builder()
                .title("Original")
                .description("Original")
                .nodes(List.of())
                .edges(List.of())
                .build();
        String originalId = management.createMindmap(createRequest).getId();

        // Act
        MindmapUpdateTitleAndDescriptionRequest updateRequest = MindmapUpdateTitleAndDescriptionRequest.builder()
                .title("Updated")
                .build();
        management.updateTitleAndDescriptionMindmap(originalId, updateRequest);

        // Assert - ID should not change in database
        Mindmap updated = repository.findById(originalId).orElseThrow();
        assertThat(updated.getId()).isEqualTo(originalId);
    }

    // ========== GET/READ TESTS ==========

    @Test
    void getMindmap_returnsPersistedData() {
        // Arrange - Create a mindmap with specific data
        MindmapCreateRequest request = MindmapCreateRequest.builder()
                .title("Specific Mindmap")
                .description("Specific Description")
                .nodes(List.of(MindmapNodeDto.builder().type("test").extraFields(Map.of("key", "value")).build()))
                .edges(List.of())
                .build();
        String id = management.createMindmap(request).getId();

        // Act
        MindmapDto result = management.getMindmap(id);

        // Assert - Verify it matches what's in database
        Mindmap dbMindmap = repository.findById(id).orElseThrow();
        assertThat(result.getId()).isEqualTo(dbMindmap.getId());
        assertThat(result.getTitle()).isEqualTo(dbMindmap.getTitle());
        assertThat(result.getDescription()).isEqualTo(dbMindmap.getDescription());
        assertThat(result.getNodes()).hasSize(dbMindmap.getNodes().size());
    }

    @Test
    void getMindmap_whenNotInDatabase_throwsResourceNotFoundException() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> management.getMindmap("non-existent-id"));

        // Verify it's not in database
        assertThat(repository.findById("non-existent-id")).isEmpty();
    }

    @Test
    void getAllMindmaps_paginated_returnsCorrectPageFromDatabase() {
        // Arrange - Create 25 mindmaps
        for (int i = 0; i < 25; i++) {
            MindmapCreateRequest request = MindmapCreateRequest.builder()
                    .title("Mindmap " + i)
                    .description("Description " + i)
                    .nodes(List.of())
                    .edges(List.of())
                    .build();
            management.createMindmap(request);
        }

        // Act - Get first page
        MindmapCollectionRequest pageRequest = MindmapCollectionRequest.builder().page(0).size(10).build();
        PaginatedResponseDto<MindmapListResponseDto> page1 = management.getAllMindmaps(pageRequest);

        // Assert
        assertThat(page1.getData()).hasSize(10);
        assertThat(page1.getPagination().getTotalItems()).isEqualTo(25);
        assertThat(page1.getPagination().getTotalPages()).isEqualTo(3);

        // Verify database count matches
        assertThat(repository.count()).isEqualTo(25);
    }

    // ========== REPOSITORY TESTS ==========

    @Test
    void repository_saveAndRetrieve_preservesNodesEdgesAndExtraFields() {
        // Arrange
        Mindmap m = Mindmap.builder()
                .title("Test")
                .description("Desc")
                .nodes(List.of(MindmapNode.builder().id("n1").type("type").extraFields(Map.of("k", "v")).build()))
                .edges(List.of(MindmapEdge.builder().id("e1").type("type").extraFields(Map.of("a", 1)).build()))
                .build();

        // Act
        Mindmap saved = repository.save(m);

        // Assert
        assertThat(saved.getId()).isNotNull();
        Mindmap found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getNodes()).hasSize(1);
        assertThat(found.getEdges()).hasSize(1);
        assertThat(found.getNodes().get(0).getId()).isEqualTo("n1");
        assertThat(found.getEdges().get(0).getId()).isEqualTo("e1");
        assertThat(found.getNodes().get(0).getExtraFields()).containsEntry("k", "v");
        assertThat(found.getEdges().get(0).getExtraFields()).containsEntry("a", 1);
    }

    @Test
    void repository_save_withComplexExtraFields_preservesDataTypes() {
        // Arrange
        Map<String, Object> nodeExtraFields = Map.of("stringField",
                "text value",
                "intField",
                42,
                "doubleField",
                3.14,
                "boolField",
                true,
                "listField",
                List.of("a", "b", "c"));

        Mindmap mindmap = Mindmap.builder()
                .title("Complex Fields Test")
                .description("Testing various data types")
                .nodes(List.of(MindmapNode.builder().id("n1").type("complex").extraFields(nodeExtraFields).build()))
                .edges(List.of())
                .build();

        // Act
        Mindmap saved = repository.save(mindmap);
        Mindmap retrieved = repository.findById(saved.getId()).orElseThrow();

        // Assert
        MindmapNode retrievedNode = retrieved.getNodes().get(0);
        assertThat(retrievedNode.getExtraFields().get("stringField")).isEqualTo("text value");
        assertThat(retrievedNode.getExtraFields().get("intField")).isEqualTo(42);
        assertThat(retrievedNode.getExtraFields().get("doubleField")).isEqualTo(3.14);
        assertThat(retrievedNode.getExtraFields().get("boolField")).isEqualTo(true);
        assertThat(retrievedNode.getExtraFields().get("listField")).isInstanceOf(List.class);
    }

    @Test
    void repository_save_withEmptyNodesAndEdges_persistsCorrectly() {
        // Arrange
        Mindmap mindmap = Mindmap.builder()
                .title("Empty Collections")
                .description("Test")
                .nodes(Collections.emptyList())
                .edges(Collections.emptyList())
                .build();

        // Act
        Mindmap saved = repository.save(mindmap);
        Mindmap retrieved = repository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(retrieved.getNodes()).isEmpty();
        assertThat(retrieved.getEdges()).isEmpty();
        assertThat(retrieved.getTitle()).isEqualTo("Empty Collections");
    }

    @Test
    void repository_save_withManyNodesAndEdges_persistsAll() {
        // Arrange
        List<MindmapNode> nodes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            nodes.add(MindmapNode.builder()
                    .id("n" + i)
                    .type("node" + i)
                    .extraFields(Map.of("index", i, "name", "Node " + i))
                    .build());
        }

        List<MindmapEdge> edges = new ArrayList<>();
        for (int i = 0; i < 49; i++) {
            edges.add(MindmapEdge.builder()
                    .id("e" + i)
                    .type("edge" + i)
                    .extraFields(Map.of("source", "n" + i, "target", "n" + (i + 1)))
                    .build());
        }

        Mindmap mindmap = Mindmap.builder()
                .title("Large Mindmap")
                .description("With many nodes and edges")
                .nodes(nodes)
                .edges(edges)
                .build();

        // Act
        Mindmap saved = repository.save(mindmap);
        Mindmap retrieved = repository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(retrieved.getNodes()).hasSize(50);
        assertThat(retrieved.getEdges()).hasSize(49);
        assertThat(retrieved.getNodes().get(0).getId()).isEqualTo("n0");
        assertThat(retrieved.getNodes().get(49).getId()).isEqualTo("n49");
        assertThat(retrieved.getEdges().get(0).getExtraFields()).containsEntry("source", "n0");
    }

    @Test
    void repository_update_preservesIdAndCreatedAt() {
        // Arrange
        Mindmap original = Mindmap.builder()
                .title("Original")
                .description("Original Desc")
                .nodes(List.of())
                .edges(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Mindmap saved = repository.save(original);
        String originalId = saved.getId();
        LocalDateTime originalCreatedAt = saved.getCreatedAt();

        // Act - Update the mindmap
        saved.setTitle("Updated Title");
        saved.setDescription("Updated Description");
        saved.setUpdatedAt(LocalDateTime.now());
        Mindmap updated = repository.save(saved);

        // Assert
        assertThat(updated.getId()).isEqualTo(originalId);
        assertThat(updated.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void repository_findAll_withPagination_returnsCorrectPage() {
        // Arrange
        for (int i = 0; i < 25; i++) {
            Mindmap m = Mindmap.builder()
                    .title("Mindmap " + i)
                    .description("Desc " + i)
                    .nodes(List.of())
                    .edges(List.of())
                    .createdAt(LocalDateTime.now().minusDays(25 - i))
                    .build();
            repository.save(m);
        }

        // Act
        Page<Mindmap> page1 = repository.findAll(PageRequest.of(0, 10));
        Page<Mindmap> page2 = repository.findAll(PageRequest.of(1, 10));
        Page<Mindmap> page3 = repository.findAll(PageRequest.of(2, 10));

        // Assert
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page3.getContent()).hasSize(5);
        assertThat(page1.getTotalElements()).isEqualTo(25);
        assertThat(page1.getTotalPages()).isEqualTo(3);
    }

    @Test
    void repository_findAll_withSort_returnsOrderedResults() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Mindmap m1 = Mindmap.builder()
                .title("First")
                .description("")
                .nodes(List.of())
                .edges(List.of())
                .createdAt(now.minusDays(2))
                .build();
        Mindmap m2 = Mindmap.builder()
                .title("Second")
                .description("")
                .nodes(List.of())
                .edges(List.of())
                .createdAt(now.minusDays(1))
                .build();
        Mindmap m3 = Mindmap.builder()
                .title("Third")
                .description("")
                .nodes(List.of())
                .edges(List.of())
                .createdAt(now)
                .build();

        repository.save(m1);
        repository.save(m2);
        repository.save(m3);

        // Act - Sort by createdAt DESC
        List<Mindmap> descResults = repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        // Assert
        assertThat(descResults).hasSize(3);
        assertThat(descResults.get(0).getTitle()).isEqualTo("Third");
        assertThat(descResults.get(1).getTitle()).isEqualTo("Second");
        assertThat(descResults.get(2).getTitle()).isEqualTo("First");
    }

    @Test
    void repository_existsById_returnsCorrectValue() {
        // Arrange
        Mindmap mindmap = Mindmap.builder()
                .title("Exists Test")
                .description("Test")
                .nodes(List.of())
                .edges(List.of())
                .build();
        Mindmap saved = repository.save(mindmap);

        // Act & Assert
        assertThat(repository.existsById(saved.getId())).isTrue();
        assertThat(repository.existsById("non-existent-id")).isFalse();
    }

    @Test
    void repository_existsByTitle_returnsCorrectValue() {
        // Arrange
        Mindmap mindmap = Mindmap.builder()
                .title("Unique Title")
                .description("Test")
                .nodes(List.of())
                .edges(List.of())
                .build();
        repository.save(mindmap);

        // Act & Assert
        assertThat(repository.existsByTitle("Unique Title")).isTrue();
        assertThat(repository.existsByTitle("Non Existent Title")).isFalse();
    }

    @Test
    void repository_findByTitleContainingIgnoreCase_returnsMatchingResults() {
        // Arrange
        repository.save(
                Mindmap.builder().title("Java Tutorial").description("").nodes(List.of()).edges(List.of()).build());
        repository.save(
                Mindmap.builder().title("Python Guide").description("").nodes(List.of()).edges(List.of()).build());
        repository.save(
                Mindmap.builder().title("JavaScript Basics").description("").nodes(List.of()).edges(List.of()).build());

        // Act
        Page<Mindmap> javaResults = repository.findByTitleContainingIgnoreCase("java", PageRequest.of(0, 10));
        Page<Mindmap> pythonResults = repository.findByTitleContainingIgnoreCase("python", PageRequest.of(0, 10));

        // Assert
        assertThat(javaResults.getContent()).hasSize(2); // Java Tutorial and JavaScript Basics
        assertThat(pythonResults.getContent()).hasSize(1);
        assertThat(pythonResults.getContent().get(0).getTitle()).isEqualTo("Python Guide");
    }

    @Test
    void repository_findByTitleContainingIgnoreCase_isCaseInsensitive() {
        // Arrange
        repository.save(
                Mindmap.builder().title("Test Mindmap").description("").nodes(List.of()).edges(List.of()).build());

        // Act
        Page<Mindmap> upperCase = repository.findByTitleContainingIgnoreCase("TEST", PageRequest.of(0, 10));
        Page<Mindmap> lowerCase = repository.findByTitleContainingIgnoreCase("test", PageRequest.of(0, 10));
        Page<Mindmap> mixedCase = repository.findByTitleContainingIgnoreCase("TeSt", PageRequest.of(0, 10));

        // Assert
        assertThat(upperCase.getContent()).hasSize(1);
        assertThat(lowerCase.getContent()).hasSize(1);
        assertThat(mixedCase.getContent()).hasSize(1);
    }

    @Test
    void repository_delete_removesEntity() {
        // Arrange
        Mindmap mindmap = Mindmap.builder()
                .title("To Delete")
                .description("Test")
                .nodes(List.of())
                .edges(List.of())
                .build();
        Mindmap saved = repository.save(mindmap);
        String id = saved.getId();

        // Act
        repository.deleteById(id);

        // Assert
        Optional<Mindmap> found = repository.findById(id);
        assertThat(found).isEmpty();
        assertThat(repository.existsById(id)).isFalse();
    }

    @Test
    void repository_count_returnsCorrectCount() {
        // Arrange
        assertThat(repository.count()).isEqualTo(0);

        for (int i = 0; i < 10; i++) {
            repository.save(
                    Mindmap.builder().title("Mindmap " + i).description("").nodes(List.of()).edges(List.of()).build());
        }

        // Act & Assert
        assertThat(repository.count()).isEqualTo(10);
    }

    @Test
    void repository_save_withSpecialCharactersInFields_persistsCorrectly() {
        // Arrange
        Mindmap mindmap = Mindmap.builder()
                .title("Title with special chars: @#$%^&*()")
                .description("Description with unicode: 你好世界 🌍")
                .nodes(List.of(MindmapNode.builder()
                        .id("n1")
                        .type("special")
                        .extraFields(Map.of("emoji", "😀", "chinese", "中文"))
                        .build()))
                .edges(List.of())
                .build();

        // Act
        Mindmap saved = repository.save(mindmap);
        Mindmap retrieved = repository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(retrieved.getTitle()).isEqualTo("Title with special chars: @#$%^&*()");
        assertThat(retrieved.getDescription()).contains("你好世界");
        assertThat(retrieved.getNodes().get(0).getExtraFields()).containsEntry("emoji", "😀");
    }

    @Test
    void repository_save_withNullDescription_handlesGracefully() {
        // Arrange
        Mindmap mindmap = Mindmap.builder()
                .title("Title Only")
                .description(null)
                .nodes(List.of())
                .edges(List.of())
                .build();

        // Act
        Mindmap saved = repository.save(mindmap);
        Mindmap retrieved = repository.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(retrieved.getTitle()).isEqualTo("Title Only");
        assertThat(retrieved.getDescription()).isNull();
    }
}
