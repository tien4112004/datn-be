package com.datn.datnbe.document.infrastructure.entity.valueobject;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlideBackground {
    @Field("type")
    String type;

    @Field("color")
    String color;
}