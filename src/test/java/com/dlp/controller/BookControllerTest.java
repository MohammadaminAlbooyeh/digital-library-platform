package com.dlp.controller;

import com.dlp.model.dto.BookDetailDTO;
import com.dlp.model.entity.Book;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.BookCatalogService;
import com.dlp.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookCatalogService bookCatalogService;

    @MockBean
    private SearchService searchService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private com.dlp.security.JwtService jwtService;

    @MockBean
    private com.dlp.security.CustomUserDetailsService customUserDetailsService;

    private Book sampleBook() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Spring in Action");
        book.setDescription("A great book");
        book.setPrice(new BigDecimal("29.99"));
        book.setPageCount(500);
        book.setIsbn("978-1234567890");
        book.setCoverImageUrl("https://cdn.example.com/covers/1.jpg");
        book.setContentFileUrl("spring-in-action.pdf");
        book.setFormat("PDF");
        book.setPublishedYear(2024);
        return book;
    }

    @Test
    void listReturnsAllBooks() throws Exception {
        when(bookCatalogService.listBooks()).thenReturn(List.of(sampleBook()));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Spring in Action"));
    }

    @Test
    void getDetailReturnsBookDetailDto() throws Exception {
        Book book = sampleBook();
        BookDetailDTO dto = new BookDetailDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setPrice(book.getPrice());
        dto.setDescription(book.getDescription());
        dto.setAuthors(Set.of("Craig Walls"));
        when(bookCatalogService.getBook(1L)).thenReturn(book);
        when(currentUserProvider.maybeCurrentUser()).thenReturn(java.util.Optional.empty());
        when(searchService.toDetail(book, null)).thenReturn(dto);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring in Action"))
                .andExpect(jsonPath("$.authors[0]").value("Craig Walls"));
    }

    @Test
    void createReturnsCreatedBook() throws Exception {
        Book saved = sampleBook();
        saved.setId(42L);
        when(bookCatalogService.createBook(any(Book.class))).thenReturn(saved);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Spring in Action\",\"price\":29.99,\"pageCount\":500}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("Spring in Action"));
    }
}
