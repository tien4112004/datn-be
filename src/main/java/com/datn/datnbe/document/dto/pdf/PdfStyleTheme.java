package com.datn.datnbe.document.dto.pdf;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PdfStyleTheme {

    /** Liberation Serif (Times New Roman look), compact — default. */
    CLASSIC("LiberationSerif", "13px", "1.5", "fonts/LiberationSerif-Regular.ttf", "fonts/LiberationSerif-Bold.ttf"),

    /** NotoSans, generous spacing — friendlier for younger students. */
    FRIENDLY("NotoSans", "14px", "1.7", "fonts/NotoSans-Regular.ttf", "fonts/NotoSans-Bold.ttf"),

    /** NotoSans, tight spacing — fits more content per page. */
    COMPACT("NotoSans", "11px", "1.35", "fonts/NotoSans-Regular.ttf", "fonts/NotoSans-Bold.ttf");

    private final String fontFamily;
    private final String fontSize;
    private final String lineHeight;
    private final String regularFontPath;
    private final String boldFontPath;
}
