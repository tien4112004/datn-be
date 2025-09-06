package com.datn.datnbe.document.entity.valueobject;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Slide {
    @Field("id")
    String id;

    @Field("elements")
    List<SlideElement> elements;

    @Field("background")
    SlideBackground background;
}
