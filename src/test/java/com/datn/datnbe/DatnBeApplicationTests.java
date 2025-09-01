package com.datn.datnbe;

import com.datn.datnbe.ai.config.TestImageModelConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DatnBeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(value = "test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Import(TestImageModelConfig.class)
class DatnBeApplicationTests {

    @Test
    void contextLoads() {
    }

}
