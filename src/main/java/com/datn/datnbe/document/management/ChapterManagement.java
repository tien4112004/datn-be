package com.datn.datnbe.document.management;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.datn.datnbe.document.dto.response.ChapterResponse;
import com.datn.datnbe.document.repository.ChapterRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChapterManagement {

    private final ChapterRepository chapterRepository;

    public List<ChapterResponse> getAllChapters(String grade, String subject) {
        return chapterRepository.findAllByGradeAndSubject(grade, subject)
                .stream()
                .map(ChapterResponse::fromChapter)
                .collect(Collectors.toList());
    }
}
