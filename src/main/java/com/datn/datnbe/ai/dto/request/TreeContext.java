package com.datn.datnbe.ai.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class TreeContext {
    String mindmapId;
    String rootNodeId;
    int currentLevel;
    String parentContent;
    List<String> siblingContents;

    // Enhanced context for better AI responses
    String mindmapTitle;
    String mindmapDescription;
    String rootNodeContent;
    List<String> fullAncestryPath; // Ordered from root to immediate parent
    List<String> existingChildrenContents; // Current children of the node being expanded
    String grade;
    String subject;
}
