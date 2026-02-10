package com.datn.datnbe.cms.entity;

import com.datn.datnbe.cms.enums.LinkedResourceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "post_linked_resources")
public class PostLinkedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "post_id", nullable = false, length = 36)
    String postId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 32)
    LinkedResourceType resourceType;

    @Column(name = "resource_id", nullable = false, length = 36)
    String resourceId;

    @Column(name = "permission_level", length = 16)
    @Builder.Default
    String permissionLevel = "view";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Date createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Post post;
}
