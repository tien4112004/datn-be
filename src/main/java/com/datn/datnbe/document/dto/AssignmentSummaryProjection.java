package com.datn.datnbe.document.dto;

/**
 * Projection interface for fetching only id and title from assignments.
 * Assignments don't have thumbnails, so this only includes id and title.
 */
public interface AssignmentSummaryProjection {
    String getId();
    String getTitle();
}
