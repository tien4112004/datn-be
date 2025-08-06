package com.datn.document.entity;

import com.datn.document.entity.valueobject.Slide;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
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
    
    @Field("slides")
    List<Slide> slides;
    
    @Field("createdAt")
    LocalDateTime createdAt;
    
    @Field("updatedAt")
    LocalDateTime updatedAt;
}
