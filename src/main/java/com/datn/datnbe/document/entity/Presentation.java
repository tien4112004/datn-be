package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.valueobject.Slide;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @Field("slides")
    List<Slide> slides;

    @Field("createdAt")
    LocalDateTime createdAt;

    @Field("updatedAt")
    @LastModifiedDate
    LocalDateTime updatedAt;

    @Field("isParsed")
    Boolean isParsed;

    @Field("metadata")
    private Map<String, Object> metadata = new java.util.HashMap<>();

    @Field(name = "deleted_at")
    LocalDate deletedAt;

    @JsonAnySetter
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @JsonAnyGetter
    public java.util.Map<String, Object> getMetadata() {
        return metadata;
    }
}
