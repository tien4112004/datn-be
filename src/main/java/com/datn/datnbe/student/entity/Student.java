package com.datn.datnbe.student.entity;


import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Student {

    @Id
    @Column(name = "id", nullable = false)
    @Builder.Default
    String id = UUID.randomUUID().toString();

    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name = "enrollment_date")
    Date enrollmentDate;

    @Column(name = "address", length = 255)
    String address;

    @Column(name = "parent_contact_email", length = 100)
    String parentContactEmail;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Date createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Date updatedAt;

    @Column(name = "gender", length = 10)
    String gender;

    @Column(name = "parent_name")
    String parentName;

    @Column(name = "parent_contact_phone", length = 15)
    String parentPhone;
}
