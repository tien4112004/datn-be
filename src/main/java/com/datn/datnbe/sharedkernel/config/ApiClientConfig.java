package com.datn.datnbe.sharedkernel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ApiClientConfig {
    @Value("${app.api-client.timeout}")
    private Integer MAX_TIMEOUT;

    @Value("${app.api-client.max-in-memory-size}")
    private Integer MAX_IN_MEMORY_SIZE; // 10 MB

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(MAX_TIMEOUT); // 30 seconds
        factory.setReadTimeout(MAX_TIMEOUT); // 30 seconds

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }

    @Bean(value = "basicWebClient")
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create().protocol(HttpProtocol.HTTP11);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();
    }
}
