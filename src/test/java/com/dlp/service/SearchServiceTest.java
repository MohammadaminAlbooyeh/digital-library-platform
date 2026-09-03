package com.dlp.service;

import com.dlp.model.dto.BookDetailDTO;
import com.dlp.model.dto.SearchRequest;
import com.dlp.model.entity.Author;
import com.dlp.model.entity.Book;
import com.dlp.model.entity.Category;
import com.dlp.repository.BookRepository;
import com.dlp.repository.UserLibraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private UserLibraryRepository userLibraryRepository;

    private SearchService service;

    @BeforeEach
    void setUp() {
        service = new SearchService(bookRepository, userLibraryRepository);
    }

    private Page<Book> emptyPage() {
        return new PageImpl<>(List.of());
    }

    @Test
    void categoryQueryRoutesToCategoryLookup() {
        SearchRequest req = new SearchRequest();
        req.setCategory("Science Fiction");
        when(bookRepository.findByCategoryName(eq("Science Fiction"), any(Pageable.class)))
                .thenReturn(emptyPage());

        service.search(req);

        verify(bookRepository).findByCategoryName(eq("Science Fiction"), any(Pageable.class));
        verify(bookRepository, never()).search(any(), any());
    }

    @Test
    void blankQueryFallsBackToFindAll() {
        SearchRequest req = new SearchRequest();
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(emptyPage());

        service.search(req);

        verify(bookRepository).findAll(any(Pageable.class));
    }

    @Test
    void textQueryIsTrimmedAndSortApplied() {
        SearchRequest req = new SearchRequest();
        req.setQuery("  space  ");
        req.setSortBy("price");
        req.setOrder("asc");
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(bookRepository.search(eq("space"), pageable.capture())).thenReturn(emptyPage());

        service.search(req);

        Sort.Order order = pageable.getValue().getSort().getOrderFor("price");
        assertNotNull(order);
        assertTrue(order.isAscending());
    }

    @Test
    void toDetailMapsFieldsAndOwnershipFlag() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Stellar Drift");
        book.setPrice(new BigDecimal("12.99"));
        Author a = new Author(); a.setName("Ava Starling");
        Category c = new Category(); c.setName("Science Fiction");
        book.setAuthors(Set.of(a));
        book.setCategories(Set.of(c));
        when(userLibraryRepository.existsByUserIdAndContentIdAndContentType(5L, 1L, "BOOK"))
                .thenReturn(true);

        BookDetailDTO dto = service.toDetail(book, 5L);

        assertEquals("Stellar Drift", dto.getTitle());
        assertEquals(Set.of("Ava Starling"), dto.getAuthors());
        assertEquals(Set.of("Science Fiction"), dto.getCategories());
        assertTrue(dto.isOwned());
    }

    @Test
    void toDetailSkipsOwnershipWhenNoUser() {
        Book book = new Book();
        book.setId(1L);
        book.setPrice(BigDecimal.ONE);

        BookDetailDTO dto = service.toDetail(book, null);

        assertFalse(dto.isOwned());
        verifyNoInteractions(userLibraryRepository);
    }
}
