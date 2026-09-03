package com.dlp.service;

import com.dlp.model.entity.Audiobook;
import com.dlp.model.entity.Book;
import com.dlp.repository.AudiobookRepository;
import com.dlp.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookCatalogServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private AudiobookRepository audiobookRepository;

    private BookCatalogService service;

    @BeforeEach
    void setUp() {
        service = new BookCatalogService(bookRepository, audiobookRepository);
    }

    @Test
    void getBookReturnsWhenPresent() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertSame(book, service.getBook(1L));
    }

    @Test
    void getBookThrowsWhenMissing() {
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getBook(2L));
    }

    @Test
    void getAudiobookThrowsWhenMissing() {
        when(audiobookRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getAudiobook(3L));
    }

    @Test
    void createBookDelegatesToRepository() {
        Book book = new Book();
        when(bookRepository.save(book)).thenReturn(book);

        assertSame(book, service.createBook(book));
    }

    @Test
    void listAudiobooksForBookDelegates() {
        Audiobook a = new Audiobook();
        when(audiobookRepository.findByBookId(9L)).thenReturn(List.of(a));

        assertEquals(1, service.listAudiobooksForBook(9L).size());
    }
}
