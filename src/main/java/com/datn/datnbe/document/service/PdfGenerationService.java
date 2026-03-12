package com.datn.datnbe.document.service;

import com.datn.datnbe.document.dto.pdf.PdfStyleTheme;
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

    private final TemplateEngine templateEngine;

    public PdfGenerationService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] renderTemplate(String templateName, Context context) {
        return renderTemplate(templateName, context, PdfStyleTheme.CLASSIC);
    }

    public byte[] renderTemplate(String templateName, Context context, PdfStyleTheme theme) {
        String html = templateEngine.process(templateName, context);
        return renderHtml(html, theme);
    }

    public byte[] renderHtml(String html, PdfStyleTheme theme) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            // openhtmltopdf requires well-formed XML; strip the HTML5 doctype
            // that Thymeleaf's HTML mode emits (<!doctype html> is not valid XML).
            String xhtml = html.replaceFirst("(?i)<!doctype[^>]*>", "");
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, null);
            registerFonts(builder, theme);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed", e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    /**
     * Registers fonts for the given theme. Always registers NotoSans as a
     * fallback for Vietnamese characters that may be missing in serif fonts.
     */
    private void registerFonts(PdfRendererBuilder builder, PdfStyleTheme theme) {
        // Primary theme fonts
        loadFont(builder, theme.getRegularFontPath(), theme.getFontFamily(), 400, false);
        loadFont(builder, theme.getBoldFontPath(), theme.getFontFamily(), 700, false);

        // Always register NotoSans as Vietnamese fallback (skip if theme already uses it)
        if (!theme.getRegularFontPath().contains("NotoSans")) {
            loadFont(builder, "fonts/NotoSans-Regular.ttf", "NotoSans", 400, false);
            loadFont(builder, "fonts/NotoSans-Bold.ttf", "NotoSans", 700, false);
        }
    }

    private void loadFont(PdfRendererBuilder builder, String path, String family, int weight, boolean italic) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            byte[] fontBytes = is.readAllBytes();
            builder.useFont(() -> new java.io.ByteArrayInputStream(fontBytes),
                    family,
                    weight,
                    italic ? PdfRendererBuilder.FontStyle.ITALIC : PdfRendererBuilder.FontStyle.NORMAL,
                    true);
        } catch (Exception e) {
            log.warn("Could not load font '{}' from '{}': {}", family, path, e.getMessage());
        }
    }
}
