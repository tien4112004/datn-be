package com.datn.datnbe.document.management;

import com.datn.datnbe.document.mapper.PresentationEntityMapper;
import com.datn.datnbe.document.mapper.SlideElementMapper;
import com.datn.datnbe.document.mapper.SlideEntityMapper;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackageClasses = {PresentationEntityMapper.class, SlideEntityMapper.class, SlideElementMapper.class})
public class TestConfig {
    // This configuration will ensure the mappers are loaded
    
    @Bean
    public SecurityContextUtils securityContextUtils() {
        return mock(SecurityContextUtils.class);
    }
}
