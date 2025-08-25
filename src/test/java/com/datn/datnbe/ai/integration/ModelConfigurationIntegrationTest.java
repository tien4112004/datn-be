package com.datn.datnbe.ai.integration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.containers.PostgreSQLContainer;

import com.datn.datnbe.ai.entity.ModelConfigurationEntity;
import com.datn.datnbe.ai.repository.interfaces.ModelConfigurationRepo;
import com.datn.datnbe.testcontainers.BaseIntegrationTest;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ModelConfigurationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ModelConfigurationRepo modelConfigurationRepo;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostgreSQLContainer<?> postgreSQLContainer;

    @LocalServerPort
    private int port;

    private ModelConfigurationEntity testModel1;
    private ModelConfigurationEntity testModel2;

    @BeforeEach
    void setUpTestData() {
        // Verify container is running
        assertThat(postgreSQLContainer.isRunning()).isTrue();
        assertThat(postgreSQLContainer.getJdbcUrl()).isNotNull();

        // Create test data
        testModel1 = new ModelConfigurationEntity();
        testModel1.setModelName("gpt-4-test");
        testModel1.setDisplayName("GPT-4 Test Model");
        testModel1.setProvider("OpenAI");
        testModel1.setEnabled(true);
        testModel1.setDefault(true);

        testModel2 = new ModelConfigurationEntity();
        testModel2.setModelName("gemini-pro-test");
        testModel2.setDisplayName("Gemini Pro Test");
        testModel2.setProvider("Google");
        testModel2.setEnabled(true);
        testModel2.setDefault(false);
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
        modelConfigurationRepo.save(testModel1);

        // When
        List<ModelConfigurationEntity> models = modelConfigurationRepo.getModels();
        ModelConfigurationEntity retrievedModel = modelConfigurationRepo.getModelByName("gpt-4-test");

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
        modelConfigurationRepo.save(testModel1);

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
        modelConfigurationRepo.save(testModel1);
        ModelConfigurationEntity savedModel = modelConfigurationRepo.getModelByName("gpt-4-test");

        // When
        boolean isEnabled = modelConfigurationRepo.isModelEnabled(savedModel.getModelId());

        // Then
        assertThat(isEnabled).isTrue();
    }

    @Test
    void setEnabled_ShouldUpdateModelStatus() {
        // Given
        testModel1.setDefault(false);
        modelConfigurationRepo.save(testModel1);
        ModelConfigurationEntity savedModel = modelConfigurationRepo.getModelByName("gpt-4-test");

        // When
        modelConfigurationRepo.setEnabled(savedModel.getModelId(), false);

        // Then
        boolean isEnabled = modelConfigurationRepo.isModelEnabled(savedModel.getModelId());
        assertThat(isEnabled).isFalse();
    }

    @Test
    void setDefault_ShouldUpdateDefaultStatus() {
        // Given
        modelConfigurationRepo.save(testModel1);
        modelConfigurationRepo.save(testModel2);
        
        ModelConfigurationEntity model1 = modelConfigurationRepo.getModelByName("gpt-4-test");
        ModelConfigurationEntity model2 = modelConfigurationRepo.getModelByName("gemini-pro-test");

        // When - Set model2 as default
        modelConfigurationRepo.setDefault(model2.getModelId(), true);

        // Then
        ModelConfigurationEntity updatedModel2 = modelConfigurationRepo.getModelById(model2.getModelId());
        assertThat(updatedModel2.isDefault()).isTrue();
    }

    @Test
    void getModels_WithMultipleModels_ShouldReturnAllModels() {
        // Given
        modelConfigurationRepo.save(testModel1);
        modelConfigurationRepo.save(testModel2);

        // When
        List<ModelConfigurationEntity> models = modelConfigurationRepo.getModels();

        // Then
        assertThat(models).hasSize(2);
        assertThat(models)
            .extracting(ModelConfigurationEntity::getModelName)
            .containsExactlyInAnyOrder("gpt-4-test", "gemini-pro-test");
    }

    @Test
    void testWithSeededData_ShouldLoadData() {
        modelConfigurationRepo.save(testModel1);
        modelConfigurationRepo.save(testModel2);

        List<ModelConfigurationEntity> models = modelConfigurationRepo.getModels();
        assertThat(models).hasSize(2);
    }

    // @Test
    // void integrationTestWithRestEndpoint_ShouldWorkEndToEnd() {
    //     // Given
    //     modelConfigurationRepo.save(testModel1);
    //     String url = "http://localhost:" + port + "/api/models";

    //     // When 
    //     ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    //     // Then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    // }

    @Test
    void testContainerReuse_ShouldUseSameContainerInstance() {
        
        // Given 
        String containerId = postgreSQLContainer.getContainerId();
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();

        // When
        modelConfigurationRepo.save(testModel1);
        boolean exists = modelConfigurationRepo.existsByModelName("gpt-4-test");

        // Then
        assertThat(postgreSQLContainer.getContainerId()).isEqualTo(containerId);
        assertThat(postgreSQLContainer.getJdbcUrl()).isEqualTo(jdbcUrl);
        assertThat(exists).isTrue();
    }
}