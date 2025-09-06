package com.datn.datnbe.sharedkernel.idempotency.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractIdempotencyService Tests")
class AbstractIdempotencyServiceTest {

    private TestIdempotencyService service;

    @BeforeEach
    void setUp() {
        service = new TestIdempotencyService();
    }

    @Test
    @DisplayName("Should return true for valid idempotency key")
    void isValid_ValidKey_ReturnsTrue() {
        assertThat(service.isValid("valid-key")).isTrue();
        assertThat(service.isValid("another-valid-key-123")).isTrue();
    }

    @Test
    @DisplayName("Should return false for invalid idempotency keys")
    void isValid_InvalidKey_ReturnsFalse() {
        assertThat(service.isValid(null)).isFalse();
        assertThat(service.isValid("")).isFalse();
        assertThat(service.isValid("   ")).isFalse();
    }

    @Test
    @DisplayName("Should initialize new idempotency entry with correct values")
    void initialize_ValidKey_CreatesNewEntry() {
        String key = "test-key";
        
        IdempotencyKey result = service.initialize(key);
        
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo(key);
        assertThat(result.getRetryCount()).isEqualTo(0);
        assertThat(result.getStatus()).isEqualTo(IdempotencyStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should allow retry when retry count is null")
    void canRetry_NullRetryCount_ReturnsTrue() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .retryCount(null)
                .build();
        
        assertThat(service.canRetry(entity)).isTrue();
    }

    @Test
    @DisplayName("Should allow retry when retry count is below max")
    void canRetry_BelowMaxRetries_ReturnsTrue() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .retryCount(3)
                .build();
        
        assertThat(service.canRetry(entity)).isTrue();
    }

    @Test
    @DisplayName("Should not allow retry when retry count equals max")
    void canRetry_AtMaxRetries_ReturnsFalse() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .retryCount(5)
                .build();
        
        assertThat(service.canRetry(entity)).isFalse();
    }

    @Test
    @DisplayName("Should increment retry count from null to 1")
    void retry_NullRetryCount_SetsToOne() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .retryCount(null)
                .build();
        
        service.retry(entity);
        
        assertThat(entity.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should increment existing retry count")
    void retry_ExistingRetryCount_Increments() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .retryCount(2)
                .build();
        
        service.retry(entity);
        
        assertThat(entity.getRetryCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should mark entity as failed")
    void fail_Entity_SetsFailedStatus() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .status(IdempotencyStatus.IN_PROGRESS)
                .build();
        
        service.fail(entity);
        
        assertThat(entity.getStatus()).isEqualTo(IdempotencyStatus.FAILED);
    }

    @Test
    @DisplayName("Should return true for failed entity")
    void isFailed_FailedEntity_ReturnsTrue() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .status(IdempotencyStatus.FAILED)
                .build();
        
        assertThat(service.isFailed(entity)).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-failed entity")
    void isFailed_NonFailedEntity_ReturnsFalse() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .status(IdempotencyStatus.COMPLETED)
                .build();
        
        assertThat(service.isFailed(entity)).isFalse();
    }

    @Test
    @DisplayName("Should complete entity with result and status code")
    void complete_Entity_SetsCompletedStatusAndData() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .status(IdempotencyStatus.IN_PROGRESS)
                .build();
        String result = "{\"data\":\"test\"}";
        int statusCode = 200;
        
        service.complete(entity, result, statusCode);
        
        assertThat(entity.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(entity.getResponseData()).isEqualTo(result);
        assertThat(entity.getStatusCode()).isEqualTo(statusCode);
    }

    @Test
    @DisplayName("Should return true for completed entity")
    void isCompleted_CompletedEntity_ReturnsTrue() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .status(IdempotencyStatus.COMPLETED)
                .build();
        
        assertThat(service.isCompleted(entity)).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-completed entity")
    void isCompleted_NonCompletedEntity_ReturnsFalse() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .status(IdempotencyStatus.IN_PROGRESS)
                .build();
        
        assertThat(service.isCompleted(entity)).isFalse();
    }

    @Test
    @DisplayName("Should return true for in-progress entity")
    void isInProgress_InProgressEntity_ReturnsTrue() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .status(IdempotencyStatus.IN_PROGRESS)
                .build();
        
        assertThat(service.isInProgress(entity)).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-in-progress entity")
    void isInProgress_NonInProgressEntity_ReturnsFalse() {
        IdempotencyKey entity = IdempotencyKey.builder()
                .key("test-key")
                .status(IdempotencyStatus.COMPLETED)
                .build();
        
        assertThat(service.isInProgress(entity)).isFalse();
    }

    private static class TestIdempotencyService extends AbstractIdempotencyService {
    }
}