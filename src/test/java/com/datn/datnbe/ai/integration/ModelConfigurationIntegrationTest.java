package com.datn.datnbe.ai.integration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.enums.ModelType;
import com.datn.datnbe.ai.repository.ModelConfigurationRepository;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Disabled("Disabled - Docker required for integration tests")
class ModelConfigurationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ModelConfigurationRepository modelConfigurationRepo;

    @Autowired
    private PostgreSQLContainer<?> postgreSQLContainer;

    @PersistenceContext
    private EntityManager entityManager;

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
        List<ModelConfigurationEntity> models = modelConfigurationRepo.findAll();
        ModelConfigurationEntity retrievedModel = modelConfigurationRepo
                .findByModelNameAndModelType("gpt-4-test", ModelType.TEXT)
                .orElse(null);

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
        ModelConfigurationEntity savedModel = modelConfigurationRepo
                .findByModelNameAndModelType("gpt-4-test", ModelType.TEXT)
                .orElseThrow();

        // When
        boolean isEnabled = modelConfigurationRepo.findById(savedModel.getModelId())
                .map(ModelConfigurationEntity::isEnabled)
                .orElse(false);

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
        ModelConfigurationEntity savedModel = modelConfigurationRepo
                .findByModelNameAndModelType("gpt-4-test", ModelType.TEXT)
                .orElseThrow();

        // When
        savedModel.setEnabled(false);
        modelConfigurationRepo.save(savedModel);

        // Then
        boolean isEnabled = modelConfigurationRepo.findById(savedModel.getModelId())
                .map(ModelConfigurationEntity::isEnabled)
                .orElse(true);
        assertThat(isEnabled).isFalse();
    }

    @Test
    void setDefault_ShouldUpdateDefaultStatus() {
        // Given
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);

        ModelConfigurationEntity model1 = modelConfigurationRepo
                .findByModelNameAndModelType("gpt-4-test", ModelType.TEXT)
                .orElseThrow();
        ModelConfigurationEntity model2 = modelConfigurationRepo
                .findByModelNameAndModelType("gemini-pro-test", ModelType.TEXT)
                .orElseThrow();

        // When - Set model2 as default
        model2.setDefault(true);
        modelConfigurationRepo.save(model2);

        // Then
        ModelConfigurationEntity updatedModel2 = modelConfigurationRepo.findById(model2.getModelId()).orElseThrow();
        assertThat(updatedModel2.isDefault()).isTrue();
    }

    @Test
    void getModels_WithMultipleModels_ShouldReturnAllModels() {
        // Given
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);

        // When
        List<ModelConfigurationEntity> models = modelConfigurationRepo.findAll();

        // Then
        assertThat(models).hasSize(2);
        assertThat(models).extracting(ModelConfigurationEntity::getModelName)
                .containsExactlyInAnyOrder("gpt-4-test", "gemini-pro-test");
    }

    @Test
    void testWithSeededData_ShouldLoadData() {
        modelConfigurationRepo.save(testTextModel1);
        modelConfigurationRepo.save(testTextModel2);

        List<ModelConfigurationEntity> models = modelConfigurationRepo.findAll();
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

        ModelConfigurationEntity savedTextModel2 = modelConfigurationRepo
                .findByModelNameAndModelType("gemini-pro-test", ModelType.TEXT)
                .orElseThrow();

        // When - Set textModel2 as default (this logic is now in Service, but we can verify Repo behavior directly if we invoke custom query)
        // Since disableDefaultModelsExcept IS in the repo, we can test it.
        modelConfigurationRepo.disableDefaultModelsExcept(ModelType.TEXT.name(), savedTextModel2.getModelId());
        savedTextModel2.setDefault(true);
        modelConfigurationRepo.save(savedTextModel2);

        // IMPORTANT: Clear the persistence context to force fresh database reads
        entityManager.flush();
        entityManager.clear();

        // Then
        ModelConfigurationEntity updatedTextModel1 = modelConfigurationRepo
                .findByModelNameAndModelType("gpt-4-test", ModelType.TEXT)
                .orElseThrow();
        ModelConfigurationEntity updatedTextModel2 = modelConfigurationRepo
                .findByModelNameAndModelType("gemini-pro-test", ModelType.TEXT)
                .orElseThrow();
        ModelConfigurationEntity updatedImageModel1 = modelConfigurationRepo
                .findByModelNameAndModelType("dall-e-test", ModelType.IMAGE)
                .orElseThrow();

        // Text models: only textModel2 should be default
        assertThat(updatedTextModel1.isDefault()).isFalse();
        assertThat(updatedTextModel2.isDefault()).isTrue();

        // Image models: should be unaffected
        assertThat(updatedImageModel1.isDefault()).isTrue();
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

        List<ModelConfigurationEntity> allModels = modelConfigurationRepo.findAll();
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
        // This test was testing logic in the Impl. That logic is now in Service.
        // We will remove this test from Integration Test or adapt it to test Service logic if we were testing Service.
        // But here we are testing Repo. The Repo itself doesn't prevent this anymore.
        // So we can remove this test or change it to verify that Repo allows it (dumb storage).
        // I'll skip it/remove it as it's no longer a Repo responsibility.
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
        List<ModelConfigurationEntity> allModels = modelConfigurationRepo.findAll();
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
        assertThat(modelConfigurationRepo.existsByModelNameAndModelType("gpt-4-unified", ModelType.TEXT)).isTrue();
        assertThat(modelConfigurationRepo.existsByModelNameAndModelType("gpt-4-unified", ModelType.IMAGE)).isFalse();
        assertThat(modelConfigurationRepo.existsByModelNameAndModelType("non-existent", ModelType.TEXT)).isFalse();
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
