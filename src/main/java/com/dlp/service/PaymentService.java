package com.dlp.service;

import com.dlp.messaging.PurchaseEventProducer;
import com.dlp.model.entity.Audiobook;
import com.dlp.model.entity.Book;
import com.dlp.model.entity.Transaction;
import com.dlp.model.entity.User;
import com.dlp.model.enums.AccessType;
import com.dlp.model.enums.ContentType;
import com.dlp.repository.BookRepository;
import com.dlp.repository.AudiobookRepository;
import com.dlp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final AudiobookRepository audiobookRepository;
    private final LibraryService libraryService;
    private final PurchaseEventProducer purchaseEventProducer;

    public PaymentService(TransactionRepository transactionRepository,
                          BookRepository bookRepository,
                          AudiobookRepository audiobookRepository,
                          LibraryService libraryService,
                          PurchaseEventProducer purchaseEventProducer) {
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
        this.audiobookRepository = audiobookRepository;
        this.libraryService = libraryService;
        this.purchaseEventProducer = purchaseEventProducer;
    }

    @Transactional
    public Transaction purchase(User user, ContentType contentType, Long contentId, String paypalPaymentId) {
        BigDecimal amount = resolvePrice(contentType, contentId);
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setContentType(contentType.name());
        transaction.setContentId(contentId);
        transaction.setAmount(amount);
        transaction.setStatus("COMPLETED");
        transaction.setPaypalPaymentId(paypalPaymentId);
        transaction = transactionRepository.save(transaction);

        libraryService.addToLibrary(user, contentId, contentType, AccessType.OWNED);
        purchaseEventProducer.publishPurchase(user.getId(), contentType.name(), contentId, amount);
        return transaction;
    }

    private BigDecimal resolvePrice(ContentType contentType, Long contentId) {
        if (contentType == ContentType.BOOK) {
            Book book = bookRepository.findById(contentId)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found"));
            return book.getPrice();
        }
        Audiobook audio = audiobookRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Audiobook not found"));
        return audio.getPrice();
    }
}

