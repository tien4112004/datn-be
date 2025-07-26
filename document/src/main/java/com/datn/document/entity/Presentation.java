package com.datn.document.entity;

import java.util.List;

import com.datn.document.entity.valueObject.Slide;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * Entity for Presentation
 */
public class Presentation {
    
    // maybe remove cause MongoDB will handle this
    private String id;
    private String title;
    
    // Will be replaced with actual slides
    private List<Slide> slides;

}
