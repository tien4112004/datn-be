package com.datn.datnbe.document.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class PdfGenerationService {

    private static final String FONT_FAMILY = "NotoSans";
    private static final String FONT_REGULAR = "fonts/NotoSans-Regular.ttf";
    private static final String FONT_BOLD = "fonts/NotoSans-Bold.ttf";

    private final TemplateEngine templateEngine;

    public PdfGenerationService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] renderTemplate(String templateName, Context context) {
        String html = templateEngine.process(templateName, context);
        return renderHtml(html);
    }

    public byte[] renderHtml(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            registerFonts(builder);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void registerFonts(PdfRendererBuilder builder) {
        loadFont(builder, FONT_REGULAR, FONT_FAMILY, 400, false);
        loadFont(builder, FONT_BOLD, FONT_FAMILY, 700, false);
    }

    private void loadFont(PdfRendererBuilder builder, String path, String family, int weight, boolean italic) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            byte[] fontBytes = is.readAllBytes();
            builder.useFont(() -> new java.io.ByteArrayInputStream(fontBytes),
                    family,
                    weight,
                    italic
                            ? com.openhtmltopdf.pdfboxout.PdfRendererBuilder.FontStyle.ITALIC
                            : com.openhtmltopdf.pdfboxout.PdfRendererBuilder.FontStyle.NORMAL,
                    true);
        } catch (Exception e) {
            log.warn("Could not load font '{}' from classpath path '{}': {}", family, path, e.getMessage());
        }
    }
}
