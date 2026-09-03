package com.dlp.service;

import com.dlp.messaging.PurchaseEventProducer;
import com.dlp.model.entity.Audiobook;
import com.dlp.model.entity.Book;
import com.dlp.model.entity.Transaction;
import com.dlp.model.entity.User;
import com.dlp.model.enums.AccessType;
import com.dlp.model.enums.ContentType;
import com.dlp.repository.AudiobookRepository;
import com.dlp.repository.BookRepository;
import com.dlp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private BookRepository bookRepository;
    @Mock private AudiobookRepository audiobookRepository;
    @Mock private LibraryService libraryService;
    @Mock private PurchaseEventProducer purchaseEventProducer;

    private PaymentService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PaymentService(transactionRepository, bookRepository,
                audiobookRepository, libraryService, purchaseEventProducer);
        user = new User();
        user.setId(7L);
    }

    @Test
    void purchaseBookPricesFromBookAddsToLibraryAndPublishesEvent() {
        Book book = new Book();
        book.setId(3L);
        book.setPrice(new BigDecimal("12.99"));
        when(bookRepository.findById(3L)).thenReturn(Optional.of(book));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction txn = service.purchase(user, ContentType.BOOK, 3L, "PAY-1");

        assertEquals(new BigDecimal("12.99"), txn.getAmount());
        assertEquals("COMPLETED", txn.getStatus());
        assertEquals("PAY-1", txn.getPaypalPaymentId());
        verify(libraryService).addToLibrary(user, 3L, ContentType.BOOK, AccessType.OWNED);
        verify(purchaseEventProducer).publishPurchase(7L, "BOOK", 3L, new BigDecimal("12.99"));
    }

    @Test
    void purchaseAudiobookPricesFromAudiobook() {
        Audiobook audio = new Audiobook();
        audio.setId(5L);
        audio.setPrice(new BigDecimal("16.99"));
        when(audiobookRepository.findById(5L)).thenReturn(Optional.of(audio));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction txn = service.purchase(user, ContentType.AUDIOBOOK, 5L, null);

        assertEquals(new BigDecimal("16.99"), txn.getAmount());
        verify(purchaseEventProducer).publishPurchase(eq(7L), eq("AUDIOBOOK"), eq(5L), any());
    }

    @Test
    void purchaseUnknownBookThrows() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.purchase(user, ContentType.BOOK, 99L, null));
        verify(transactionRepository, never()).save(any());
    }
}
