package com.datn.datnbe.document.management;

import com.datn.datnbe.document.mapper.PresentationEntityMapper;
import com.datn.datnbe.document.mapper.SlideElementMapper;
import com.datn.datnbe.document.mapper.SlideEntityMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackageClasses = {
        PresentationEntityMapper.class,
        SlideEntityMapper.class,
        SlideElementMapper.class
})
public class TestConfig {
    // This configuration will ensure the mappers are loaded
}
