package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.valueobject.Slide;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "presentations")
public class Presentation {

    @Id
    String id;

    @Field("title")
    String title;

//    @Embedded
//    @DBRef
    @Field("slides")
    List<Slide> slides;

    @Field("createdAt")
    LocalDateTime createdAt;

    @Field("updatedAt")
    @LastModifiedDate
    LocalDateTime updatedAt;
}
