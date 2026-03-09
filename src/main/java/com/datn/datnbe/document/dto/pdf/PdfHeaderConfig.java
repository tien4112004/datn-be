package com.datn.datnbe.document.dto.pdf;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PdfHeaderConfig {

    /** Optional school or institution name shown above the title. */
    String schoolName;

    /** Show chapter field in header metadata. Default false. */
    Boolean showChapter = false;

    /** Show description below the title. Default false. */
    Boolean showDescription = false;
}
