package com.datn.datnbe.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalendarEventDto {

    private String id;
    private String title;
    private EventType type;
    private Instant date;
    private String classId;
    private String className;
    private String relatedId;
    private String description;
    private String status;

    public enum EventType {
        DEADLINE, GRADING_REMINDER, SCHEDULED_POST, ASSIGNMENT_RETURNED
    }
}
