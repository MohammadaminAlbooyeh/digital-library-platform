package com.dlp.util;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FileFormatValidator {

    private static final Set<String> BOOK_FORMATS = Set.of("pdf", "epub", "mobi");
    private static final Set<String> AUDIO_FORMATS = Set.of("mp3", "aac", "m4a", "ogg");

    public boolean isSupportedBookFormat(String extension) {
        return BOOK_FORMATS.contains(normalize(extension));
    }

    public boolean isSupportedAudioFormat(String extension) {
        return AUDIO_FORMATS.contains(normalize(extension));
    }

    public boolean isSupported(String extension) {
        return isSupportedBookFormat(extension) || isSupportedAudioFormat(extension);
    }

    public String contentTypeFor(String extension) {
        String ext = normalize(extension);
        return switch (ext) {
            case "pdf" -> "application/pdf";
            case "epub" -> "application/epub+zip";
            case "mobi" -> "application/x-mobipocket-ebook";
            case "mp3" -> "audio/mpeg";
            case "aac" -> "audio/aac";
            case "m4a" -> "audio/mp4";
            case "ogg" -> "audio/ogg";
            default -> "application/octet-stream";
        };
    }

    private String normalize(String extension) {
        return extension == null ? "" : extension.toLowerCase().replace(".", "");
    }
}

