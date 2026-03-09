package com.datn.datnbe.document.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Smoke test: verifies the Thymeleaf + OpenHTMLToPDF pipeline produces
 * a valid PDF containing Vietnamese diacritics without requiring a Spring context.
 *
 * Run with: ./gradlew test --tests "*.PdfGenerationSmokeTest"
 */
class PdfGenerationSmokeTest {

    private PdfGenerationService pdfGenerationService;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        pdfGenerationService = new PdfGenerationService(engine);
    }

    @Test
    void smokeTest_assignmentTemplateShouldProduceNonEmptyPdf() throws IOException {
        Context context = new Context();
        context.setVariable("testMessage", "Kiểm tra tiếng Việt: ắ ộ ễ ư ơ ề ổ ặ ẫ ụ ẹ ỉ ọ ừ ứ");

        byte[] pdf = pdfGenerationService.renderTemplate("assignment-template", context);

        assertThat(pdf).isNotEmpty();
        // PDF files always start with the %PDF- header
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        // Write to /tmp for manual visual inspection of Vietnamese characters
        Path outputPath = Path.of("/tmp/smoke-test-output.pdf");
        Files.write(outputPath, pdf);
        System.out.println("PDF written to: " + outputPath.toAbsolutePath());
    }
}
