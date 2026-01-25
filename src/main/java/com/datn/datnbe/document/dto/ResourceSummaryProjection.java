package com.datn.datnbe.document.dto;

/**
 * Projection interface for fetching only title and thumbnail from resources.
 * Used to avoid loading full entity data (like slides JSON) when only summary is needed.
 */
public interface ResourceSummaryProjection {
    String getId();
    String getTitle();
    String getThumbnail();
}
