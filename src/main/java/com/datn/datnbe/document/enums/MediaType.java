package com.datn.datnbe.document.enums;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaType {
    IMAGE("images", List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg")),
    VIDEO("videos", List.of("mp4", "mov", "avi", "mkv", "wmv", "flv", "webm")),
    DOCUMENT("documents", List.of("pdf", "doc", "docx", "txt", "rtf", "odt")),
    AUDIO("audio", List.of("mp3", "wav", "flac", "aac", "ogg", "wma"));

    private final String folder;
    private final List<String> extensions;

    public static String getFolderByExtension(String extension) {
        for (MediaType mediaType : MediaType.values()) {
            if (mediaType.extensions.contains(extension.toLowerCase())) {
                return mediaType.folder;
            }
        }
        return null;
    }

    public static String getFileExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            throw new IllegalArgumentException("Invalid file extension in filename: " + filename);
        }
        return filename.substring(idx + 1);
    }
}
