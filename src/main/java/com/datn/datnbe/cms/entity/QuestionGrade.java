package com.datn.datnbe.cms.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionGrade {
    String questionId;
    Integer points;        
    Integer maxPoints;     
    String feedback;       
    Boolean isAutoGraded;  
}
