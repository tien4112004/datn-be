package com.datn.datnbe.sharedkernel.idempotency.api;

import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract service providing default implementations for idempotency operations.
 * Extend this class to customize behavior for specific use cases.
 */
@Slf4j
public abstract class AbstractIdempotencyService {

    private static final int MAX_RETRIES = 5;

    /**
     * Checks if the given key is valid.
     *
     * @param key the idempotency key
     * @return true if the key is valid, false otherwise
     */
    public boolean isValid(String key) {
        return key != null && !key.trim().isEmpty();
    }

    /**
     * Invalidates the given key.
     *
     * @param key the idempotency key
     * @throws AppException always
     */
    public void invalidate(String key) {
        throw new AppException(ErrorCode.IDEMPOTENCY_KEY_INVALID);
    }

    /**
     * Initializes a new idempotency entry with the given key.
     *
     * @param key the idempotency key
     * @return a new IdempotencyKeyEntity instance
     */
    public IdempotencyKey initialize(String key) {
        log.info("Creating new idempotency entry for key: {}", key);
        return IdempotencyKey.builder().key(key).retryCount(0).status(IdempotencyStatus.IN_PROGRESS).build();
    }

    /**
     * Checks if the given entity can retry based on its retry count.
     *
     * @param entity the idempotency key entity
     * @return true if retries are allowed, false otherwise
     */
    public boolean canRetry(IdempotencyKey entity) {
        if (entity.getRetryCount() == null) {
            return true;
        }
        return entity.getRetryCount() < MAX_RETRIES;
    }

    /**
     * Increments the retry count for the given entity.
     *
     * @param entity the idempotency key entity
     */
    public void retry(IdempotencyKey entity) {
        log.info("Retrying idempotent request with key: {}", entity.getKey());
        if (entity.getRetryCount() == null) {
            entity.setRetryCount(0);
        }
        entity.setRetryCount(entity.getRetryCount() + 1);
    }

    /**
     * Marks the given entity as failed.
     *
     * @param entity the idempotency key entity
     */
    public void fail(IdempotencyKey entity) {
        entity.setStatus(IdempotencyStatus.FAILED);
    }

    /**
     * Checks if the given entity is marked as failed.
     *
     * @param entity the idempotency key entity
     * @return true if the entity is failed, false otherwise
     */
    public boolean isFailed(IdempotencyKey entity) {
        return entity.getStatus() == IdempotencyStatus.FAILED;
    }

    /**
     * Marks the given entity as completed and sets the response data and status code.
     *
     * @param entity the idempotency key entity
     * @param result the response data
     * @param statusCode the HTTP status code
     */
    public void complete(IdempotencyKey entity, String result, int statusCode) {
        entity.setStatus(IdempotencyStatus.COMPLETED);
        entity.setResponseData(result);
        entity.setStatusCode(statusCode);
    }

    /**
     * Checks if the given entity is marked as completed.
     *
     * @param entity the idempotency key entity
     * @return true if the entity is completed, false otherwise
     */
    public boolean isCompleted(IdempotencyKey entity) {
        return entity.getStatus() == IdempotencyStatus.COMPLETED;
    }

    /**
     * Checks if the given entity is in progress.
     *
     * @param entity the idempotency key entity
     * @return true if the entity is in progress, false otherwise
     */
    public boolean isInProgress(IdempotencyKey entity) {
        return entity.getStatus() == IdempotencyStatus.IN_PROGRESS;
    }

    /**
     * Logs that the request with the given idempotency key is already in progress.
     *
     * @param entity the idempotency key entity
     */
    public void processing(IdempotencyKey entity) {
        log.info("Request with this idempotency key is already in progress.");
        // No-op by default
    }

    /**
     * Returns the actual key to be used for idempotency operations.
     * Override this method to customize key transformation.
     *
     * @param originalKey the original idempotency key
     * @return the transformed idempotency key
     */
    public String getActualKey(String originalKey) {
        return originalKey;
    }
}
