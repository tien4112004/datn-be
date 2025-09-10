package com.datn.datnbe.ai.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.PostgreSQLContainer;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ModelConfigurationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ModelConfigurationRepo modelConfigurationRepo;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostgreSQLContainer<?> postgreSQLContainer;

    @PersistenceContext
    private EntityManager entityManager;

    @LocalServerPort
    private int port;

    private ModelConfigurationEntity testTextModel1;
    private ModelConfigurationEntity testTextModel2;
    private ModelConfigurationEntity testTextModel3;

    private ModelConfigurationEntity testImageModel1;
    private ModelConfigurationEntity testImageModel2;

    @BeforeEach
    void setUpTestData() {
        // Verify container is running
        assertThat(postgreSQLContainer.isRunning()).isTrue();
        assertThat(postgreSQLContainer.getJdbcUrl()).isNotNull();

        // Create test data
        testTextModel1 = new ModelConfigurationEntity();
        testTextModel1.setModelName("gpt-4-test");
        testTextModel1.setDisplayName("GPT-4 Test Model");
        testTextModel1.setProvider("OpenAI");
        testTextModel1.setModelType(ModelType.TEXT);
        testTextModel1.setEnabled(true);
        testTextModel1.setDefault(true);

        testTextModel2 = new ModelConfigurationEntity();
        testTextModel2.setModelName("gemini-pro-test");
        testTextModel2.setDisplayName("Gemini Pro Test");
        testTextModel2.setProvider("Google");
        testTextModel2.setModelType(ModelType.TEXT);
        testTextModel2.setEnabled(true);
        testTextModel2.setDefault(false);

        testTextModel3 = new ModelConfigurationEntity();
        testTextModel3.setModelName("llama-3-test");
        testTextModel3.setDisplayName("LLaMA 3 Test Model");
        testTextModel3.setProvider("Meta");
        testTextModel3.setModelType(ModelType.TEXT);
        testTextModel3.setEnabled(true);
        testTextModel3.setDefault(false);

        testImageModel1 = new ModelConfigurationEntity();
        testImageModel1.setModelName("dall-e-test");
        testImageModel1.setDisplayName("DALL-E Test Model");
        testImageModel1.setProvider("OpenAI");
        testImageModel1.setModelType(ModelType.IMAGE);
        testImageModel1.setEnabled(true);
        testImageModel1.setDefault(true);

        testImageModel2 = new ModelConfigurationEntity();
        testImageModel2.setModelName("midjourney-test");
        testImageModel2.setDisplayName("MidJourney Test Model");
        testImageModel2.setProvider("MidJourney");
        testImageModel2.setModelType(ModelType.IMAGE);
        testImageModel2.setEnabled(true);
        testImageModel2.setDefault(false);
    }

    @Test
    void testContainerConfiguration() {
        assertThat(postgreSQLContainer.getDatabaseName()).isEqualTo("testdb");
        assertThat(postgreSQLContainer.getUsername()).isEqualTo("testuser");
        assertThat(postgreSQLContainer.getPassword()).isEqualTo("testpass");

        String expectedJdbcUrl = postgreSQLContainer.getJdbcUrl();
        assertThat(expectedJdbcUrl).contains("testdb");
    }

    @Test
    void saveAndRetrieveModel_ShouldPersistCorrectly() {
        // Given
        modelConfigurationRepo.save(testTextModel1);

        // When
        List<ModelConfigurationEntity> models = modelConfigurationRepo.getModels();
        ModelConfigurationEntity retrievedModel = modelConfigurationRepo.getModelByNameAndType("gpt-4-test",
                ModelType.TEXT);

        // Then
        assertThat(models).isNotEmpty();
        assertThat(retrievedModel).isNotNull();
        assertThat(retrievedModel.getModelName()).isEqualTo("gpt-4-test");
        assertThat(retrievedModel.getDisplayName()).isEqualTo("GPT-4 Test Model");
        assertThat(retrievedModel.getProvider()).isEqualTo("OpenAI");
        assertThat(retrievedModel.isEnabled()).isTrue();
        assertThat(retrievedModel.isDefault()).isTrue();
    }

    @Test
    void existsByModelName_WithExistingModel_ShouldReturnTrue() {
        // Given
        modelConfigurationRepo.save(testTextModel1);

        // When
        boolean exists = modelConfigurationRepo.existsByModelName("gpt-4-test");
        boolean notExists = modelConfigurationRepo.existsByModelName("non-existent-model");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void isModelEnabled_WithEnabledModel_ShouldReturnTrue() {
        // Given
        modelConfigurationRepo.save(testTextModel1);
        ModelConfigurationEntity savedModel = modelConfigurationRepo.getModelByNameAndType("gpt-4-test",
                ModelType.TEXT);

        // When
        boolean isEnabled = modelConfigurationRepo.isModelEnabled(savedModel.getModelId());

        // Then
        assertThat(isEnabled).isTrue();
    }

    @Test
    void setEnabled_ShouldUpdateModelStatus() {
        // Given - Save two models so we can disable one
        testTextModel1.setDefault(false);
        testTextModel2.setDefault(false);
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);
        ModelConfigurationEntity savedModel = modelConfigurationRepo.getModelByNameAndType("gpt-4-test",
                ModelType.TEXT);

        // When
        modelConfigurationRepo.setEnabled(savedModel.getModelId(), false);

        // Then
        boolean isEnabled = modelConfigurationRepo.isModelEnabled(savedModel.getModelId());
        assertThat(isEnabled).isFalse();
    }

    @Test
    void setDefault_ShouldUpdateDefaultStatus() {
        // Given
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);

        ModelConfigurationEntity model1 = modelConfigurationRepo.getModelByNameAndType("gpt-4-test", ModelType.TEXT);
        ModelConfigurationEntity model2 = modelConfigurationRepo.getModelByNameAndType("gemini-pro-test",
                ModelType.TEXT);

        // When - Set model2 as default
        modelConfigurationRepo.setDefault(model2.getModelId(), true);

        // Then
        ModelConfigurationEntity updatedModel2 = modelConfigurationRepo.getModelById(model2.getModelId());
        assertThat(updatedModel2.isDefault()).isTrue();
    }

    @Test
    void getModels_WithMultipleModels_ShouldReturnAllModels() {
        // Given
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);

        // When
        List<ModelConfigurationEntity> models = modelConfigurationRepo.getModels();

        // Then
        assertThat(models).hasSize(2);
        assertThat(models).extracting(ModelConfigurationEntity::getModelName)
                .containsExactlyInAnyOrder("gpt-4-test", "gemini-pro-test");
    }

    @Test
    void testWithSeededData_ShouldLoadData() {
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);

        List<ModelConfigurationEntity> models = modelConfigurationRepo.getModels();
        assertThat(models).hasSize(2);
    }

    @Test
    void setDefault_ShouldOnlyAffectSameTypeModels() {
        // Given - Save text models
        modelConfigurationRepo.save(testTextModel1); // default = true
        modelConfigurationRepo.save(testTextModel2); // default = false
        modelConfigurationRepo.save(testTextModel3); // default = false

        // Save image models
        modelConfigurationRepo.save(testImageModel1); // default = true
        modelConfigurationRepo.save(testImageModel2); // default = false

        ModelConfigurationEntity savedTextModel2 = modelConfigurationRepo.getModelByNameAndType("gemini-pro-test",
                ModelType.TEXT);

        // When - Set textModel2 as default (should unset textModel1 but not affect
        // image models)
        modelConfigurationRepo.setDefault(savedTextModel2.getModelId(), true);

        // IMPORTANT: Clear the persistence context to force fresh database reads
        entityManager.flush();
        entityManager.clear();

        // Then
        ModelConfigurationEntity updatedTextModel1 = modelConfigurationRepo.getModelByNameAndType("gpt-4-test",
                ModelType.TEXT);
        ModelConfigurationEntity updatedTextModel2 = modelConfigurationRepo.getModelByNameAndType("gemini-pro-test",
                ModelType.TEXT);
        ModelConfigurationEntity updatedImageModel1 = modelConfigurationRepo.getModelByNameAndType("dall-e-test",
                ModelType.IMAGE);

        // Text models: only textModel2 should be default
        assertThat(updatedTextModel1.isDefault()).isFalse();
        assertThat(updatedTextModel2.isDefault()).isTrue();

        // Image models: should be unaffected
        assertThat(updatedImageModel1.isDefault()).isTrue();
    }

    @Test
    void getTextModels_ShouldReturnOnlyTextModels() {
        // Given
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);
        modelConfigurationRepo.save(testImageModel1);
        modelConfigurationRepo.save(testImageModel2);

        // When
        List<ModelConfigurationEntity> textModels = modelConfigurationRepo.getModelsByType(ModelType.TEXT);

        // Then
        assertThat(textModels).hasSize(2);
        assertThat(textModels).extracting(ModelConfigurationEntity::getModelType).containsOnly(ModelType.TEXT);
        assertThat(textModels).extracting(ModelConfigurationEntity::getModelName)
                .containsExactlyInAnyOrder("gpt-4-test", "gemini-pro-test");
    }

    @Test
    void getImageModels_ShouldReturnOnlyImageModels() {
        // Given
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);
        modelConfigurationRepo.save(testImageModel1);
        modelConfigurationRepo.save(testImageModel2);

        // When
        List<ModelConfigurationEntity> imageModels = modelConfigurationRepo.getModelsByType(ModelType.IMAGE);

        // Then
        assertThat(imageModels).hasSize(2);
        assertThat(imageModels).extracting(ModelConfigurationEntity::getModelType).containsOnly(ModelType.IMAGE);
        assertThat(imageModels).extracting(ModelConfigurationEntity::getModelName)
                .containsExactlyInAnyOrder("dall-e-test", "midjourney-test");
    }

    @Test
    void uniqueConstraint_ShouldAllowSameModelNameForDifferentTypes() {
        // Given - Create two models with same name but different types
        ModelConfigurationEntity gptTextModel = new ModelConfigurationEntity();
        gptTextModel.setModelName("gpt-4-unified");
        gptTextModel.setDisplayName("GPT-4 Text");
        gptTextModel.setProvider("OpenAI");
        gptTextModel.setModelType(ModelType.TEXT);
        gptTextModel.setEnabled(true);
        gptTextModel.setDefault(false);

        ModelConfigurationEntity gptImageModel = new ModelConfigurationEntity();
        gptImageModel.setModelName("gpt-4-unified");
        gptImageModel.setDisplayName("GPT-4 Vision");
        gptImageModel.setProvider("OpenAI");
        gptImageModel.setModelType(ModelType.IMAGE);
        gptImageModel.setEnabled(true);
        gptImageModel.setDefault(false);

        // When & Then - Both should save successfully
        modelConfigurationRepo.save(gptTextModel);
        modelConfigurationRepo.save(gptImageModel);

        List<ModelConfigurationEntity> allModels = modelConfigurationRepo.getModels();
        assertThat(allModels).hasSize(2);
        assertThat(allModels).extracting(ModelConfigurationEntity::getModelName).containsOnly("gpt-4-unified");
        assertThat(allModels).extracting(ModelConfigurationEntity::getModelType)
                .containsExactlyInAnyOrder(ModelType.TEXT, ModelType.IMAGE);
    }

    @Test
    void deleteByModelName_WithDefaultModel_ShouldWork() {
        // Given
        modelConfigurationRepo.save(testTextModel1); // default = true
        modelConfigurationRepo.save(testTextModel2); // default = false

        // When
        modelConfigurationRepo.deleteByModelName("gpt-4-test");

        // Then
        assertThat(modelConfigurationRepo.existsByModelName("gpt-4-test")).isFalse();
        assertThat(modelConfigurationRepo.existsByModelName("gemini-pro-test")).isTrue();
    }

    @Test
    void setEnabled_WithDefaultModel_ShouldPreventDisabling() {
        // Given
        testTextModel1.setDefault(true);
        modelConfigurationRepo.save(testTextModel1);
        ModelConfigurationEntity savedModel = modelConfigurationRepo.getModelByNameAndType("gpt-4-test",
                ModelType.TEXT);

        // When & Then
        assertThat(savedModel.isDefault()).isTrue();

        // Attempting to disable a default model should throw an exception
        assertThrows(Exception.class, () -> {
            modelConfigurationRepo.setEnabled(savedModel.getModelId(), false);
        });
    }

    @Test
    void setDefaultForModelType_WithDisabledModel_ShouldThrowException() {
        // Given
        testTextModel1.setEnabled(false);
        testTextModel1.setDefault(false);
        modelConfigurationRepo.save(testTextModel1);
        ModelConfigurationEntity savedModel = modelConfigurationRepo.getModelByNameAndType("gpt-4-test",
                ModelType.TEXT);

        // When & Then
        assertThrows(AppException.class, () -> {
            modelConfigurationRepo.setDefault(savedModel.getModelId(), true);
        });
    }

    // ===============================
    // Tests for type-specific default behavior
    // ===============================

    @Test
    void multipleDefaultsInDifferentTypes_ShouldBeAllowed() {
        // Given
        testTextModel1.setDefault(true);
        testImageModel1.setDefault(true);

        // When
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testImageModel1);

        // Then
        List<ModelConfigurationEntity> allModels = modelConfigurationRepo.getModels();
        List<ModelConfigurationEntity> defaultModels = allModels.stream()
                .filter(ModelConfigurationEntity::isDefault)
                .toList();

        assertThat(defaultModels).hasSize(2);
        assertThat(defaultModels).extracting(ModelConfigurationEntity::getModelType)
                .containsExactlyInAnyOrder(ModelType.TEXT, ModelType.IMAGE);
    }

    @Test
    void existsByModelNameAndType_ShouldWorkCorrectly() {
        // Given
        ModelConfigurationEntity gptTextModel = new ModelConfigurationEntity();
        gptTextModel.setModelName("gpt-4-unified");
        gptTextModel.setDisplayName("GPT-4 Text");
        gptTextModel.setProvider("OpenAI");
        gptTextModel.setModelType(ModelType.TEXT);
        gptTextModel.setEnabled(true);
        gptTextModel.setDefault(false);

        modelConfigurationRepo.save(gptTextModel);

        // When & Then
        assertThat(modelConfigurationRepo.existsByModelNameAndType("gpt-4-unified", "TEXT")).isTrue();
        assertThat(modelConfigurationRepo.existsByModelNameAndType("gpt-4-unified", "IMAGE")).isFalse();
        assertThat(modelConfigurationRepo.existsByModelNameAndType("non-existent", "TEXT")).isFalse();
    }

    // ===============================
    // Additional integration tests
    // ===============================

    @Test
    void testContainerReuse_ShouldUseSameContainerInstance() {

        // Given
        String containerId = postgreSQLContainer.getContainerId();
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();

        // When
        modelConfigurationRepo.save(testTextModel1);
        boolean exists = modelConfigurationRepo.existsByModelName("gpt-4-test");

        // Then
        assertThat(postgreSQLContainer.getContainerId()).isEqualTo(containerId);
        assertThat(postgreSQLContainer.getJdbcUrl()).isEqualTo(jdbcUrl);
        assertThat(exists).isTrue();
    }
}
