package com.datn.datnbe.student.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegeneratePasswordRequest {
    List<String> students;
}
