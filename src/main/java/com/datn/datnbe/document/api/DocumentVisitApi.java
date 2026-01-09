package com.datn.datnbe.document.api;

import com.datn.datnbe.document.dto.response.RecentDocumentDto;

import java.util.List;

public interface DocumentVisitApi {

    /**
     * Get recent documents (6-7 items) visited by the current user
     */
    List<RecentDocumentDto> getRecentDocuments();

    /**
     * Get recent documents with custom limit
     */
    List<RecentDocumentDto> getRecentDocuments(int limit);

    /**
     * Track a document visit
     */
    void trackDocumentVisit(String userId, String documentId, String documentType);
}
