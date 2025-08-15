package com.datn.datnbe.ai.infrastructure.config.datasource;

import com.datn.datnbe.ai.infrastructure.entity.ModelConfigurationEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration("modelDataSourceConfig")
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.datn.datnbe.ai.infrastructure.repository.impl.jpa",
        entityManagerFactoryRef = "modelEntityManagerFactory",
        transactionManagerRef = "modelTransactionManager"
)
public class ModelDataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.models")
    public DataSourceProperties ModelsDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource modelsDataSource() {
        return ModelsDatasourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean modelEntityManagerFactory(
            @Qualifier("modelsDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages(ModelConfigurationEntity.class)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager modelTransactionManager(
            @Qualifier("modelEntityManagerFactory") LocalContainerEntityManagerFactoryBean modelsEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(modelsEntityManagerFactory.getObject()));
    }
}
