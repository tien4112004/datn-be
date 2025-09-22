package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.valueobject.Slide;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "presentations")
@SQLDelete(sql = "UPDATE students SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Presentation {

    @Id
    String id;

    @Field("title")
    String title;

    // @Embedded
    // @DBRef
    @Field("slides")
    List<Slide> slides;

    @Field("createdAt")
    LocalDateTime createdAt;

    @Field("updatedAt")
    @LastModifiedDate
    LocalDateTime updatedAt;

    @Field("isParsed")
    Boolean isParsed;

    @Field("meta_data")
    Object metaData;

    @Field(name = "deleted_at")
    LocalDate deletedAt;
}
