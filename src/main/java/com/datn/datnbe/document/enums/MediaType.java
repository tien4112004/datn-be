package com.datn.datnbe.document.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum MediaType {
    IMAGE("images", List.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg")),
    VIDEO("videos", List.of("mp4", "mov", "avi", "mkv", "wmv", "flv", "webm")),
    DOCUMENT("documents", List.of("pdf", "doc", "docx", "txt", "rtf", "odt")),
    AUDIO("audio", List.of("mp3", "wav", "flac", "aac", "ogg", "wma"));

    private final String folder;
    private final List<String> extensions;

    public static Optional<MediaType> getByExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedExt = extension.toLowerCase().trim();
        return java.util.Arrays.stream(MediaType.values())
                .filter(type -> type.extensions.contains(normalizedExt))
                .findFirst();
    }

    public static String getFolderByExtension(String extension) {
        return getByExtension(extension).map(MediaType::getFolder).orElse(null);
    }

    public static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            throw new IllegalArgumentException("Invalid file extension in filename: " + filename);
        }
        return filename.substring(idx + 1);
    }

    public boolean isImage() {
        return this == IMAGE;
    }
}
