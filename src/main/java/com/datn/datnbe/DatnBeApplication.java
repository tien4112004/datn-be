package com.datn.datnbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication(exclude = {HttpClientAutoConfiguration.class, RestClientAutoConfiguration.class,
        RestTemplateAutoConfiguration.class})
public class DatnBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatnBeApplication.class, args);
    }
}
