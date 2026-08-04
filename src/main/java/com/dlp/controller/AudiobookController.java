package com.dlp.controller;

import com.dlp.model.entity.Audiobook;
import com.dlp.service.BookCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audiobooks")
public class AudiobookController {

    private final BookCatalogService catalogService;

    public AudiobookController(BookCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Audiobook> get(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getAudiobook(id));
    }

    @GetMapping("/byBook/{bookId}")
    public List<Audiobook> byBook(@PathVariable Long bookId) {
        return catalogService.listAudiobooksForBook(bookId);
    }
}

