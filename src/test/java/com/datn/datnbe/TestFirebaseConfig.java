package com.datn.datnbe;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Mock Firebase configuration for tests
 * This configuration mocks Firebase beans to avoid real Firebase initialization during tests
 */
@TestConfiguration
@Profile("test")
public class TestFirebaseConfig {

    /**
     * Mock FirebaseApp bean
     */
    @Bean
    @Primary
    public FirebaseApp mockFirebaseApp() {
        return Mockito.mock(FirebaseApp.class);
    }

    /**
     * Mock FirebaseAuth bean
     */
    @Bean
    @Primary
    public FirebaseAuth mockFirebaseAuth() {
        return Mockito.mock(FirebaseAuth.class);
    }
}
