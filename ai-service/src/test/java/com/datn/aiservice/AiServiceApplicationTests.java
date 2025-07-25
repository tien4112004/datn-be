package com.datn.aiservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(value = "/application-test.yml")
class AiServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
