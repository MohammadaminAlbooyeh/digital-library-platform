package com.dlp.service;

import com.dlp.model.entity.Book;
import com.dlp.model.entity.Publisher;
import com.dlp.model.entity.Transaction;
import com.dlp.repository.BookRepository;
import com.dlp.repository.PublisherRepository;
import com.dlp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublisherRoyaltyServiceTest {

    @Mock private PublisherRepository publisherRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BookRepository bookRepository;

    private PublisherRoyaltyService service;

    @BeforeEach
    void setUp() {
        service = new PublisherRoyaltyService(publisherRepository, transactionRepository, bookRepository);
    }

    private Transaction txn(String amount) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        return t;
    }

    @Test
    void sumsRoyaltiesAcrossBooksAtPublisherRate() {
        Publisher p = new Publisher();
        p.setId(1L);
        p.setRoyaltyRate(new BigDecimal("0.50"));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(p));

        Book b1 = new Book(); b1.setId(10L);
        Book b2 = new Book(); b2.setId(11L);
        when(bookRepository.findByPublisherId(1L)).thenReturn(List.of(b1, b2));
        when(transactionRepository.findByContentIdAndContentType(10L, "BOOK"))
                .thenReturn(List.of(txn("10.00"), txn("20.00")));
        when(transactionRepository.findByContentIdAndContentType(11L, "BOOK"))
                .thenReturn(List.of(txn("30.00")));

        assertEquals(new BigDecimal("30.00"), service.computeRoyalties(1L));
    }

    @Test
    void defaultsRateToHalfWhenNull() {
        Publisher p = new Publisher();
        p.setId(2L);
        p.setRoyaltyRate(null);
        when(publisherRepository.findById(2L)).thenReturn(Optional.of(p));

        Book b = new Book(); b.setId(20L);
        when(bookRepository.findByPublisherId(2L)).thenReturn(List.of(b));
        when(transactionRepository.findByContentIdAndContentType(20L, "BOOK"))
                .thenReturn(List.of(txn("15.00")));

        assertEquals(new BigDecimal("7.50"), service.computeRoyalties(2L));
    }

    @Test
    void unknownPublisherThrows() {
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.computeRoyalties(99L));
    }
}
