package com.dlp.controller;

import com.dlp.model.dto.StreamAccessDTO;
import com.dlp.model.entity.Book;
import com.dlp.model.entity.UserLibraryItem;
import com.dlp.model.enums.ContentType;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.ContentDeliveryService;
import com.dlp.service.DrmService;
import com.dlp.service.LibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final ContentDeliveryService contentDeliveryService;
    private final DrmService drmService;
    private final CurrentUserProvider currentUserProvider;

    public LibraryController(LibraryService libraryService,
                             ContentDeliveryService contentDeliveryService,
                             DrmService drmService,
                             CurrentUserProvider currentUserProvider) {
        this.libraryService = libraryService;
        this.contentDeliveryService = contentDeliveryService;
        this.drmService = drmService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<UserLibraryItem> myLibrary() {
        return libraryService.getLibrary(currentUserProvider.currentUser());
    }

    @GetMapping("/recommendations")
    public List<Book> recommendations(@RequestParam(defaultValue = "10") int limit) {
        var user = currentUserProvider.maybeCurrentUser().orElse(null);
        return contentDeliveryService.getRecommendations(user, limit);
    }

    @PostMapping("/stream")
    public ResponseEntity<StreamAccessDTO> stream(@RequestBody Map<String, String> body) {
        ContentType contentType = ContentType.valueOf(String.valueOf(body.get("contentType")).toUpperCase());
        Long contentId = Long.valueOf(body.get("contentId"));
        String deviceFingerprint = body.get("deviceFingerprint");
        var user = currentUserProvider.currentUser();
        return ResponseEntity.ok(contentDeliveryService.getStreamAccess(
                user, contentType, contentId, deviceFingerprint));
    }

    @GetMapping("/download")
    public ResponseEntity<Map<String, String>> download(@RequestParam Long contentId,
                                                        @RequestParam String contentType) {
        var user = currentUserProvider.currentUser();
        String url = contentDeliveryService.getDownloadUrl(
                user, ContentType.valueOf(contentType.toUpperCase()), contentId);
        return ResponseEntity.ok(Map.of("url", url));
    }
}

