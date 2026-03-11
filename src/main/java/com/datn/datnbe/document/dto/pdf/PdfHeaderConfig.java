package com.datn.datnbe.document.dto.pdf;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfHeaderConfig {

    /** Use exam-style header (dept/institution + exam info) instead of centered title with badges. */
    Boolean useExamHeader = false;

    /** Tên sở/trường — top-left line 1 (e.g. "SỞ GD&ĐT TP. HỒ CHÍ MINH") */
    String departmentName;

    /** Tên cơ sở trực tiếp — top-left line 2 (e.g. "TRƯỜNG TIỂU HỌC NGUYỄN DU") */
    String institutionName;

    /** Kỳ thi - Năm học — top-right line 1 (e.g. "KIỂM TRA GIỮA KỲ I - NĂM HỌC 2025-2026") */
    String examPeriod;

    /** Thời gian — top-right line 3 (e.g. "40 phút") */
    String examDuration;

    /** Show chapter field in header metadata. Default false. */
    Boolean showChapter = false;

    /** Show description below the title. Default false. */
    Boolean showDescription = false;
}
