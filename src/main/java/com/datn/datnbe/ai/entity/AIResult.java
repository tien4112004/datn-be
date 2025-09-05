package com.datn.datnbe.ai.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;

    String result;

    Date createdAt;

    String presentationId;
}
