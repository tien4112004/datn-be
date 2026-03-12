package com.datn.datnbe.document.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.datn.datnbe.document.dto.pdf.AssignmentPdfViewModel;
import com.datn.datnbe.document.dto.pdf.PdfExportRequest;
import com.datn.datnbe.document.dto.pdf.PdfHeaderConfig;
import com.datn.datnbe.document.dto.pdf.PdfStyleTheme;
import com.datn.datnbe.document.dto.response.AssignmentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class PdfGenerationSmokeTest {

    private PdfGenerationService pdfGenerationService;
    private AssignmentPdfViewModelMapper viewModelMapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        pdfGenerationService = new PdfGenerationService(engine);
        viewModelMapper = new AssignmentPdfViewModelMapper(objectMapper);
    }

    @Test
    void classic_theme_withAnswerKeyAndExplanations() throws Exception {
        AssignmentPdfViewModel viewModel = loadSampleViewModel();

        PdfHeaderConfig headerConfig = new PdfHeaderConfig();
        headerConfig.setUseExamHeader(true);
        headerConfig.setDepartmentName("SỞ GD&ĐT TP. HỒ CHÍ MINH");
        headerConfig.setInstitutionName("TRƯỜNG TIỂU HỌC NGUYỄN DU");
        headerConfig.setExamPeriod("KIỂM TRA GIỮA KỲ I - NĂM HỌC 2025-2026");
        headerConfig.setExamDuration("40 phút");
        headerConfig.setShowChapter(true);
        headerConfig.setShowDescription(true);

        PdfExportRequest request = new PdfExportRequest();
        request.setTheme(PdfStyleTheme.CLASSIC);
        request.setHeaderConfig(headerConfig);
        request.setShowQuestionPoints(true);
        request.setShowAnswerKey(true);
        request.setShowExplanations(true);

        byte[] pdf = renderPdf(viewModel, request);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        Path out = Path.of("/tmp/assignment-classic-answerkey.pdf");
        Files.write(out, pdf);
        System.out.println("CLASSIC + answer key: " + out);
    }

    @Test
    void friendly_theme_noPoints_noAnswerKey() throws Exception {
        AssignmentPdfViewModel viewModel = loadSampleViewModel();

        PdfExportRequest request = new PdfExportRequest();
        request.setTheme(PdfStyleTheme.FRIENDLY);
        request.setShowQuestionPoints(false);
        request.setShowAnswerKey(false);

        byte[] pdf = renderPdf(viewModel, request);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

        Path out = Path.of("/tmp/assignment-friendly-nopoints.pdf");
        Files.write(out, pdf);
        System.out.println("FRIENDLY (no points): " + out);
    }

    // --- helpers ---

    private AssignmentPdfViewModel loadSampleViewModel() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/fixtures/sample-assignment.json")) {
            AssignmentResponse assignment = objectMapper.readValue(is, AssignmentResponse.class);
            return viewModelMapper.toViewModel(assignment);
        }
    }

    private byte[] renderPdf(AssignmentPdfViewModel viewModel, PdfExportRequest request) {
        Context context = new Context();
        context.setVariable("assignment", viewModel);
        context.setVariable("exportRequest", request);
        context.setVariable("theme", request.getTheme());
        String templateName = Boolean.TRUE.equals(request.getShowAnswerKey())
                ? "answer-template"
                : "assignment-template";
        return pdfGenerationService.renderTemplate(templateName, context, request.getTheme());
    }
}
