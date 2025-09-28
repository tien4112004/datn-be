package com.datn.datnbe.document.entity;

import com.datn.datnbe.document.entity.valueobject.MindmapNode;
import com.datn.datnbe.document.entity.valueobject.MindmapEdge;
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
@Document(collection = "mindmaps")
public class Mindmap {

    @Id
    String id;

    @Field("title")
    String title;

    @Field("description")
    String description;

    @Field("nodes")
    List<MindmapNode> nodes;

    @Field("edges")
    List<MindmapEdge> edges;

    @Field("createdAt")
    LocalDateTime createdAt;

    @Field("updatedAt")
    @LastModifiedDate
    LocalDateTime updatedAt;

}
