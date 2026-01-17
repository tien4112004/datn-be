package com.datn.datnbe.document.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "assignment_questions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "question_id"})})
public class AssignmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    @Column(name = "assignment_id", nullable = false, length = 36)
    String assignmentId;

    @Column(name = "question_id", nullable = false, length = 36)
    String questionId;

    @Column(name = "point")
    Double point;

    @Column(name = "\"order\"")
    Integer order;
}
