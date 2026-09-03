package com.dlp.service;

import com.dlp.model.dto.BookDetailDTO;
import com.dlp.model.dto.SearchRequest;
import com.dlp.model.entity.Book;
import com.dlp.repository.BookRepository;
import com.dlp.repository.UserLibraryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Service
public class SearchService {

    private final BookRepository bookRepository;
    private final UserLibraryRepository userLibraryRepository;

    public SearchService(BookRepository bookRepository, UserLibraryRepository userLibraryRepository) {
        this.bookRepository = bookRepository;
        this.userLibraryRepository = userLibraryRepository;
    }

    public Page<Book> search(SearchRequest request) {
        Sort sort = buildSort(request);
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            return bookRepository.findByCategoryName(request.getCategory(), pageable);
        }
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            return bookRepository.findAll(pageable);
        }
        return bookRepository.search(request.getQuery().trim(), pageable);
    }

    public BookDetailDTO toDetail(Book book, Long userId) {
        BookDetailDTO dto = new BookDetailDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setDescription(book.getDescription());
        dto.setPrice(book.getPrice());
        dto.setPageCount(book.getPageCount());
        dto.setIsbn(book.getIsbn());
        dto.setCoverImageUrl(book.getCoverImageUrl());
        dto.setFormat(book.getFormat());
        dto.setPublishedYear(book.getPublishedYear());
        dto.setAuthors(book.getAuthors().stream().map(a -> a.getName()).collect(Collectors.toSet()));
        dto.setCategories(book.getCategories().stream().map(c -> c.getName()).collect(Collectors.toSet()));
        if (userId != null) {
            dto.setOwned(userLibraryRepository.existsByUserIdAndContentIdAndContentType(
                    userId, book.getId(), "BOOK"));
        }
        return dto;
    }

    private Sort buildSort(SearchRequest request) {
        String field = switch (request.getSortBy()) {
            case "price" -> "price";
            case "title" -> "title";
            case "publishedYear" -> "publishedYear";
            default -> "id";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(request.getOrder())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}

