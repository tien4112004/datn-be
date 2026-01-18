package com.datn.datnbe.cms.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase.config-path}")
    private String firebaseConfigPath;

    @Bean
    public FirebaseApp firebaseApp(ResourceLoader resourceLoader) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        Resource resource = resourceLoader.getResource(firebaseConfigPath);

        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            logger.info("Initializing Firebase App with config: {}", firebaseConfigPath);
            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase App: {}", e.getMessage());
            throw e;
        }
    }
}
