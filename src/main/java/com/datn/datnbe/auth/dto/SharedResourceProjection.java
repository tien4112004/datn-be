package com.datn.datnbe.auth.dto;

/**
 * Projection interface for querying shared resources from the database.
 * Used by DocumentResourceMappingRepository to project shared resource data.
 * Title and thumbnail are fetched from source (presentation/mindmap tables) to avoid stale data.
 */
public interface SharedResourceProjection {
    String getId();
    String getType();
    String getOwnerId();
    String getReadersGroupId();
    String getCommentersGroupId();
}
