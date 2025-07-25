package com.datn.aiservice.config;

import com.datn.aiservice.repository.impl.ModelConfigurationImpl;
import com.datn.aiservice.repository.impl.jpa.SpringDataJPAModelConfigurationRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public ModelConfigurationImpl modelConfigurationRepository(
            SpringDataJPAModelConfigurationRepo springDataJPAModelConfigurationRepo
    ) {
        return new ModelConfigurationImpl(springDataJPAModelConfigurationRepo);
    }
}
