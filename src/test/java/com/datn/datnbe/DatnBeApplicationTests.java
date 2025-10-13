package com.datn.datnbe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(value = "test")
@TestPropertySource(locations = "classpath:application-test.yml")
class DatnBeApplicationTests {

    @Test
    void contextLoads() {
    }
}
