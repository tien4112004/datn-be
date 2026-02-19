package com.datn.datnbe.document.utils;

import com.datn.datnbe.document.entity.questiondata.BlankSegment;
import com.datn.datnbe.document.entity.questiondata.FillInBlankData;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FillInBlankParser {

    private static final Pattern BLANK_PATTERN = Pattern.compile("\\{\\{([^}]*)\\}\\}");

    public static FillInBlankData parse(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }

        List<BlankSegment> segments = new ArrayList<>();
        Matcher matcher = BLANK_PATTERN.matcher(inputText);

        int lastIndex = 0;

        while (matcher.find()) {
            String textBefore = inputText.substring(lastIndex, matcher.start());
            if (!textBefore.isEmpty()) {
                BlankSegment textSegment = BlankSegment.builder()
                        .type(BlankSegment.SegmentType.TEXT)
                        .content(textBefore)
                        .build();
                segments.add(textSegment);
            }

            String blankContent = matcher.group(1).trim();
            List<String> acceptableAnswers = parseAnswers(blankContent);

            if (!acceptableAnswers.isEmpty()) {
                String firstAnswer = acceptableAnswers.get(0);
                List<String> remainingAnswers = acceptableAnswers.size() > 1 
                    ? acceptableAnswers.subList(1, acceptableAnswers.size()) 
                    : Collections.emptyList();
                
                BlankSegment blankSegment = BlankSegment.builder()
                        .type(BlankSegment.SegmentType.BLANK)
                        .content(firstAnswer)
                        .acceptableAnswers(remainingAnswers)
                        .build();
                segments.add(blankSegment);
            }

            lastIndex = matcher.end();
        }

        // Thêm TEXT segment cuối cùng (nếu có)
        if (lastIndex < inputText.length()) {
            String textAfter = inputText.substring(lastIndex);
            if (!textAfter.isEmpty()) {
                BlankSegment textSegment = BlankSegment.builder()
                        .type(BlankSegment.SegmentType.TEXT)
                        .content(textAfter)
                        .build();
                segments.add(textSegment);
            }
        }

        log.info("Parsed {} segments from input text", segments.size());

        return FillInBlankData.builder().segments(segments).caseSensitive(false).build();
    }

    private static List<String> parseAnswers(String answerString) {
        if (answerString == null || answerString.isBlank()) {
            return Collections.emptyList();
        }

        List<String> answers = new ArrayList<>();
        String[] parts = answerString.split("\\|");

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                answers.add(trimmed);
            }
        }

        return answers;
    }

    public static boolean isValidFormat(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            return false;
        }

        int openCount = 0;
        int closeCount = 0;

        for (int i = 0; i < inputText.length() - 1; i++) {
            if (inputText.charAt(i) == '{' && inputText.charAt(i + 1) == '{') {
                openCount++;
            } else if (inputText.charAt(i) == '}' && inputText.charAt(i + 1) == '}') {
                closeCount++;
            }
        }

        return openCount == closeCount && openCount > 0;
    }
}
