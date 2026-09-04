package com.dlp.controller;

import com.dlp.model.dto.BookDetailDTO;
import com.dlp.model.entity.Book;
import com.dlp.model.entity.User;
import com.dlp.model.dto.SearchRequest;
import com.dlp.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SearchController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.dlp.service.SearchService searchService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @MockBean
    private com.dlp.security.JwtService jwtService;

    @MockBean
    private com.dlp.security.CustomUserDetailsService customUserDetailsService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user@test.com");
    }

    @Test
    void searchReturnsPageOfResults() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Spring in Action");
        BookDetailDTO dto = new BookDetailDTO();
        dto.setId(1L);
        dto.setTitle("Spring in Action");
        dto.setAuthors(java.util.Set.of("Craig Walls"));

        when(currentUserProvider.maybeCurrentUser()).thenReturn(Optional.empty());
        when(searchService.search(any(SearchRequest.class)))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(searchService.toDetail(book, null)).thenReturn(dto);

        mockMvc.perform(get("/api/search")
                        .param("q", "Spring")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Spring in Action"));
    }

    @Test
    void searchWithCategoryFiltersByCategory() throws Exception {
        Book book = new Book();
        book.setId(2L);
        book.setTitle("Clean Code");
        BookDetailDTO dto = new BookDetailDTO();
        dto.setId(2L);
        dto.setTitle("Clean Code");

        when(currentUserProvider.maybeCurrentUser()).thenReturn(Optional.empty());
        when(searchService.search(any(SearchRequest.class)))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(searchService.toDetail(book, null)).thenReturn(dto);

        mockMvc.perform(get("/api/search")
                        .param("category", "Programming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }
}
