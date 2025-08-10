package com.datn.aiservice;

import com.datn.aiservice.entity.ModelConfigurationEntity;
import com.datn.aiservice.repository.impl.jpa.ModelConfigurationJPARepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SuppressWarnings("resource")
public class ModelConfigurationCRUDIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    @Autowired
    private ModelConfigurationJPARepo modelConfigurationRepo;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        modelConfigurationRepo.deleteAll();
    }

    @Test
    void testCreateModelConfiguration() {
        // Given
        ModelConfigurationEntity entity = new ModelConfigurationEntity();
        entity.setModelName("gpt-4-test");
        entity.setDisplayName("GPT-4 Test Model");
        entity.setProvider("openai");
        entity.setEnabled(true);
        entity.setDefault(false);

        // When
        ModelConfigurationEntity saved = modelConfigurationRepo.save(entity);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getModelId()).isNotNull();
        assertThat(saved.getModelName()).isEqualTo("gpt-4-test");
        assertThat(saved.getDisplayName()).isEqualTo("GPT-4 Test Model");
        assertThat(saved.getProvider()).isEqualTo("openai");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.isDefault()).isFalse();
    }

    @Test
    void testReadModelConfiguration() {
        // Given - Create and save an entity
        ModelConfigurationEntity entity = new ModelConfigurationEntity();
        entity.setModelName("gemini-test");
        entity.setDisplayName("Gemini Test Model");
        entity.setProvider("google");
        entity.setEnabled(true);
        entity.setDefault(true);
        
        ModelConfigurationEntity saved = modelConfigurationRepo.save(entity);
        Integer savedId = saved.getModelId();

        // When
        Optional<ModelConfigurationEntity> found = modelConfigurationRepo.findById(savedId);

        // Then
        assertThat(found).isPresent();
        ModelConfigurationEntity foundEntity = found.get();
        assertThat(foundEntity.getModelId()).isEqualTo(savedId);
        assertThat(foundEntity.getModelName()).isEqualTo("gemini-test");
        assertThat(foundEntity.getDisplayName()).isEqualTo("Gemini Test Model");
        assertThat(foundEntity.getProvider()).isEqualTo("google");
        assertThat(foundEntity.isEnabled()).isTrue();
        assertThat(foundEntity.isDefault()).isTrue();
    }

    @Test
    void testUpdateModelConfiguration() {
        // Given - Create and save an entity
        ModelConfigurationEntity entity = new ModelConfigurationEntity();
        entity.setModelName("claude-test");
        entity.setDisplayName("Claude Test Model");
        entity.setProvider("anthropic");
        entity.setEnabled(true);
        entity.setDefault(false);
        
        ModelConfigurationEntity saved = modelConfigurationRepo.save(entity);
        Integer savedId = saved.getModelId();

        // When - Update the entity
        saved.setDisplayName("Claude Updated Model");
        saved.setDefault(true);
        saved.setEnabled(false);
        ModelConfigurationEntity updated = modelConfigurationRepo.save(saved);

        // Then
        assertThat(updated.getModelId()).isEqualTo(savedId);
        assertThat(updated.getModelName()).isEqualTo("claude-test"); // Unchanged
        assertThat(updated.getDisplayName()).isEqualTo("Claude Updated Model"); // Updated
        assertThat(updated.getProvider()).isEqualTo("anthropic"); // Unchanged
        assertThat(updated.isEnabled()).isFalse(); // Updated
        assertThat(updated.isDefault()).isTrue(); // Updated

        // Verify the changes are persisted
        Optional<ModelConfigurationEntity> reloaded = modelConfigurationRepo.findById(savedId);
        assertThat(reloaded).isPresent();
        ModelConfigurationEntity reloadedEntity = reloaded.get();
        assertThat(reloadedEntity.getDisplayName()).isEqualTo("Claude Updated Model");
        assertThat(reloadedEntity.isDefault()).isTrue();
        assertThat(reloadedEntity.isEnabled()).isFalse();
    }

    @Test
    void testDeleteModelConfiguration() {
        // Given - Create and save an entity
        ModelConfigurationEntity entity = new ModelConfigurationEntity();
        entity.setModelName("llama-test");
        entity.setDisplayName("Llama Test Model");
        entity.setProvider("meta");
        entity.setEnabled(true);
        entity.setDefault(false);
        
        ModelConfigurationEntity saved = modelConfigurationRepo.save(entity);
        Integer savedId = saved.getModelId();

        // Verify entity exists
        assertThat(modelConfigurationRepo.findById(savedId)).isPresent();

        // When - Delete the entity
        modelConfigurationRepo.deleteById(savedId);

        // Then
        Optional<ModelConfigurationEntity> deleted = modelConfigurationRepo.findById(savedId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testFindAllModelConfigurations() {
        // Given - Create multiple entities
        ModelConfigurationEntity entity1 = new ModelConfigurationEntity();
        entity1.setModelName("model-1");
        entity1.setDisplayName("Model 1");
        entity1.setProvider("provider1");
        entity1.setEnabled(true);
        entity1.setDefault(false);

        ModelConfigurationEntity entity2 = new ModelConfigurationEntity();
        entity2.setModelName("model-2");
        entity2.setDisplayName("Model 2");
        entity2.setProvider("provider2");
        entity2.setEnabled(false);
        entity2.setDefault(true);

        modelConfigurationRepo.save(entity1);
        modelConfigurationRepo.save(entity2);

        // When
        List<ModelConfigurationEntity> allEntities = modelConfigurationRepo.findAll();

        // Then
        assertThat(allEntities).hasSize(2);
        assertThat(allEntities)
                .extracting(ModelConfigurationEntity::getModelName)
                .containsExactlyInAnyOrder("model-1", "model-2");
    }

    @Test
    void testFindByModelName() {
        // Given
        ModelConfigurationEntity entity = new ModelConfigurationEntity();
        entity.setModelName("unique-model");
        entity.setDisplayName("Unique Model");
        entity.setProvider("unique-provider");
        entity.setEnabled(true);
        entity.setDefault(false);
        
        modelConfigurationRepo.save(entity);

        // When
        Optional<ModelConfigurationEntity> found = modelConfigurationRepo.findByModelName("unique-model");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getModelName()).isEqualTo("unique-model");
        assertThat(found.get().getDisplayName()).isEqualTo("Unique Model");
    }

    @Test
    void testFindByModelNameNotFound() {
        // When
        Optional<ModelConfigurationEntity> found = modelConfigurationRepo.findByModelName("non-existent-model");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testUniqueConstraintOnModelName() {
        // Given - Create first entity
        ModelConfigurationEntity entity1 = new ModelConfigurationEntity();
        entity1.setModelName("duplicate-name");
        entity1.setDisplayName("First Model");
        entity1.setProvider("provider1");
        entity1.setEnabled(true);
        entity1.setDefault(false);
        
        modelConfigurationRepo.save(entity1);

        // When/Then - Try to create second entity with same model name
        ModelConfigurationEntity entity2 = new ModelConfigurationEntity();
        entity2.setModelName("duplicate-name");
        entity2.setDisplayName("Second Model");
        entity2.setProvider("provider2");
        entity2.setEnabled(true);
        entity2.setDefault(false);

        // This should throw an exception due to unique constraint
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> modelConfigurationRepo.save(entity2)
        );
    }

    @Test
    void testCustomQueryUnsetDefaultForOtherModels() {
        // Given - Create multiple entities with one as default
        ModelConfigurationEntity entity1 = new ModelConfigurationEntity();
        entity1.setModelName("model-default");
        entity1.setDisplayName("Default Model");
        entity1.setProvider("provider1");
        entity1.setEnabled(true);
        entity1.setDefault(true);

        ModelConfigurationEntity entity2 = new ModelConfigurationEntity();
        entity2.setModelName("model-other");
        entity2.setDisplayName("Other Model");
        entity2.setProvider("provider2");
        entity2.setEnabled(true);
        entity2.setDefault(false);

        ModelConfigurationEntity saved1 = modelConfigurationRepo.save(entity1);
        ModelConfigurationEntity saved2 = modelConfigurationRepo.save(entity2);

        // When - Find all entities
        List<ModelConfigurationEntity> allEntities = modelConfigurationRepo.findAll();

        // Then - Verify entities are saved correctly
        assertThat(allEntities).hasSize(2);
        assertThat(allEntities)
                .extracting(ModelConfigurationEntity::getModelName)
                .containsExactlyInAnyOrder("model-default", "model-other");
        
        // Verify initial default states
        Optional<ModelConfigurationEntity> reloaded1 = modelConfigurationRepo.findById(saved1.getModelId());
        Optional<ModelConfigurationEntity> reloaded2 = modelConfigurationRepo.findById(saved2.getModelId());

        assertThat(reloaded1).isPresent();
        assertThat(reloaded2).isPresent();
        
        assertThat(reloaded1.get().isDefault()).isTrue();
        assertThat(reloaded2.get().isDefault()).isFalse();
    }
}
