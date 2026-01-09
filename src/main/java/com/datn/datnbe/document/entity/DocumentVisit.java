package com.datn.datnbe.document.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_visits", 
       indexes = {
           @Index(name = "idx_user_visited", columnList = "user_id,last_visited DESC"),
           @Index(name = "idx_document_user", columnList = "document_id,user_id", unique = true)
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentVisit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name = "document_id", nullable = false)
    String documentId;

    @Column(name = "document_type", nullable = false)
    String documentType;

    @Column(name = "last_visited", nullable = false)
    LocalDateTime lastVisited;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
