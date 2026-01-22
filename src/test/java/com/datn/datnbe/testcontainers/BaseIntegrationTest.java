package com.datn.datnbe.testcontainers;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

    static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void checkDockerAvailable() {
        Assumptions.assumeTrue(isDockerAvailable(), "Docker is not available. Skipping integration tests.");
    }

    @BeforeEach
    void cleanDatabases() {

        jdbc.execute(
                """
                        DO $$
                        DECLARE
                            stm text;
                        BEGIN
                            SELECT 'TRUNCATE TABLE ' || string_agg(format('%I.%I', schemaname, tablename), ', ') || ' RESTART IDENTITY CASCADE'
                            INTO stm
                            FROM pg_tables
                            WHERE schemaname = 'public'
                              AND tablename <> 'flyway_schema_history';

                            IF stm IS NOT NULL THEN
                                EXECUTE stm;
                            END IF;
                        END $$;
                        """);
    }
}
