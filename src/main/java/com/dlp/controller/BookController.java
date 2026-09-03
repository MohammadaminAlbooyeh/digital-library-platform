package com.dlp.controller;

import com.dlp.model.dto.BookDetailDTO;
import com.dlp.model.entity.Book;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.BookCatalogService;
import com.dlp.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookCatalogService catalogService;
    private final SearchService searchService;
    private final CurrentUserProvider currentUserProvider;

    public BookController(BookCatalogService catalogService,
                          SearchService searchService,
                          CurrentUserProvider currentUserProvider) {
        this.catalogService = catalogService;
        this.searchService = searchService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public List<Book> list() {
        return catalogService.listBooks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailDTO> get(@PathVariable Long id) {
        Book book = catalogService.getBook(id);
        Long userId = currentUserProvider.maybeCurrentUser().map(u -> u.getId()).orElse(null);
        return ResponseEntity.ok(searchService.toDetail(book, userId));
    }

    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createBook(book));
    }
}

