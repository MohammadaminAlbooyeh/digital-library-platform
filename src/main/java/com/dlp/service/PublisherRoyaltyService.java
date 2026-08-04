package com.dlp.service;

import com.dlp.model.entity.Book;
import com.dlp.model.entity.Publisher;
import com.dlp.model.entity.Transaction;
import com.dlp.repository.BookRepository;
import com.dlp.repository.PublisherRepository;
import com.dlp.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PublisherRoyaltyService {

    private final PublisherRepository publisherRepository;
    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;

    public PublisherRoyaltyService(PublisherRepository publisherRepository,
                                   TransactionRepository transactionRepository,
                                   BookRepository bookRepository) {
        this.publisherRepository = publisherRepository;
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
    }

    public BigDecimal computeRoyalties(Long publisherId) {
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        BigDecimal rate = publisher.getRoyaltyRate() == null
                ? new BigDecimal("0.50") : publisher.getRoyaltyRate();

        List<Book> books = bookRepository.findByPublisherId(publisherId);
        BigDecimal total = BigDecimal.ZERO;
        for (Book book : books) {
            for (Transaction txn : transactionRepository.findByContentIdAndContentType(book.getId(), "BOOK")) {
                total = total.add(txn.getAmount().multiply(rate));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public List<Publisher> listPublishers() {
        return publisherRepository.findAll();
    }
}

