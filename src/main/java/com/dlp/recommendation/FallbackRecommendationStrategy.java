package com.dlp.recommendation;

import com.dlp.model.entity.Book;
import com.dlp.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FallbackRecommendationStrategy {

    private static final Logger log = LoggerFactory.getLogger(FallbackRecommendationStrategy.class);

    private final BookRepository bookRepository;

    public FallbackRecommendationStrategy(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> fallbackPopular(int limit) {
        // Simple fallback: return the most recently added books as a curated list.
        var page = bookRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, limit,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        return page.getContent();
    }


    public List<Book> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return bookRepository.findAllById(ids);
    }
}

