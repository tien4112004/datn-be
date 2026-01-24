package com.datn.datnbe.document.api;

import java.util.List;

/**
 * API interface for document validation operations.
 * Provides methods to check existence of various document types.
 */
public interface DocumentValidationApi {

    /**
     * Counts the number of existing mindmaps from the provided IDs.
     *
     * @param ids the list of mindmap IDs to check
     * @return the count of existing mindmaps
     */
    long countExistingMindmaps(List<String> ids);

    /**
     * Counts the number of existing presentations from the provided IDs.
     *
     * @param ids the list of presentation IDs to check
     * @return the count of existing presentations
     */
    long countExistingPresentations(List<String> ids);

    /**
     * Counts the number of existing assignments from the provided IDs.
     *
     * @param ids the list of assignment IDs to check
     * @return the count of existing assignments
     */
    long countExistingAssignments(List<String> ids);
}
