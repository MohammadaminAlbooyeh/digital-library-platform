package com.dlp.controller;

import com.dlp.model.dto.ReadingProgressDTO;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.ReadingProgressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ReadingProgressController {

    private final ReadingProgressService progressService;
    private final CurrentUserProvider currentUserProvider;

    public ReadingProgressController(ReadingProgressService progressService,
                                     CurrentUserProvider currentUserProvider) {
        this.progressService = progressService;
        this.currentUserProvider = currentUserProvider;
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<ReadingProgressDTO> update(@PathVariable Long contentId,
                                                      @Valid @RequestBody ReadingProgressDTO dto) {
        var user = currentUserProvider.currentUser();
        return ResponseEntity.ok(progressService.updateProgress(user, contentId, dto));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ReadingProgressDTO> get(@PathVariable Long contentId) {
        var user = currentUserProvider.currentUser();
        return ResponseEntity.ok(progressService.getProgress(user, contentId));
    }
}

