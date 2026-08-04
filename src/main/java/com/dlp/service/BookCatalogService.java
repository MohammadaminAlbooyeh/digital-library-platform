package com.dlp.service;

import com.dlp.model.entity.Audiobook;
import com.dlp.model.entity.Book;
import com.dlp.repository.AudiobookRepository;
import com.dlp.repository.BookRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookCatalogService {

    private final BookRepository bookRepository;
    private final AudiobookRepository audiobookRepository;

    public BookCatalogService(BookRepository bookRepository, AudiobookRepository audiobookRepository) {
        this.bookRepository = bookRepository;
        this.audiobookRepository = audiobookRepository;
    }

    @Cacheable(value = "books", key = "#id")
    public Book getBook(Long id) {
        return bookRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Book not found: " + id));
    }

    @Cacheable(value = "bookListings")
    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = {"books", "bookListings"}, allEntries = true)
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    public Audiobook getAudiobook(Long id) {
        return audiobookRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Audiobook not found: " + id));
    }

    public List<Audiobook> listAudiobooksForBook(Long bookId) {
        return audiobookRepository.findByBookId(bookId);
    }

    public List<Book> listBooksByAuthor(String author) {
        return bookRepository.findByAuthorName(author);
    }
}

