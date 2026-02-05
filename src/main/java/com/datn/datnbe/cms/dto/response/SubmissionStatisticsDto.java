package com.datn.datnbe.cms.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmissionStatisticsDto {
    Long totalSubmissions;
    Long gradedCount;
    Long pendingCount;
    Long inProgressCount;
    Double averageScore;
    Map<String, Long> scoreDistribution;
    Double highestScore;
    Double lowestScore;
}
