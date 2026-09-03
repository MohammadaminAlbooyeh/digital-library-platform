package com.dlp.controller;

import com.dlp.model.dto.StreamAccessDTO;
import com.dlp.model.entity.Book;
import com.dlp.model.entity.User;
import com.dlp.model.entity.UserLibraryItem;
import com.dlp.model.enums.AccessType;
import com.dlp.model.enums.ContentType;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.ContentDeliveryService;
import com.dlp.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LibraryController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LibraryService libraryService;

    @MockBean
    private ContentDeliveryService contentDeliveryService;

    @MockBean
    private com.dlp.service.DrmService drmService;

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
        currentUser.setName("Test User");
    }

    private UserLibraryItem sampleItem(ContentType type, AccessType access) {
        UserLibraryItem item = new UserLibraryItem();
        item.setId(10L);
        item.setUser(currentUser);
        item.setContentId(5L);
        item.setContentType(type);
        item.setAccessType(access);
        return item;
    }

    @Test
    void myLibraryReturnsUserItems() throws Exception {
        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(libraryService.getLibrary(currentUser))
                .thenReturn(List.of(sampleItem(ContentType.BOOK, AccessType.OWNED)));

        mockMvc.perform(get("/api/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contentId").value(5))
                .andExpect(jsonPath("$[0].contentType").value("BOOK"));
    }

    @Test
    void streamReturnsStreamAccess() throws Exception {
        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        StreamAccessDTO access = StreamAccessDTO.builder()
                .contentId("5")
                .streamUrl("https://cdn.example.com/content/books/5/file.m4u8?expires=123&signature=abc")
                .token("abc123token")
                .expiresInSeconds(7200)
                .build();
        when(contentDeliveryService.getStreamAccess(currentUser, ContentType.BOOK, 5L, "device-fp"))
                .thenReturn(access);

        mockMvc.perform(post("/api/library/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"BOOK\",\"contentId\":5,\"deviceFingerprint\":\"device-fp\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value("5"))
                .andExpect(jsonPath("$.token").value("abc123token"))
                .andExpect(jsonPath("$.expiresInSeconds").value(7200));
    }

    @Test
    void streamRejectsMissingContentId() throws Exception {
        when(currentUserProvider.currentUser()).thenReturn(currentUser);

        mockMvc.perform(post("/api/library/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"BOOK\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void downloadReturnsSignedUrl() throws Exception {
        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(contentDeliveryService.getDownloadUrl(currentUser, ContentType.BOOK, 5L))
                .thenReturn("https://cdn.example.com/content/books/5/file.m4u8?expires=123&signature=abc");

        mockMvc.perform(get("/api/library/download")
                        .param("contentId", "5")
                        .param("contentType", "BOOK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://cdn.example.com/content/books/5/file.m4u8?expires=123&signature=abc"));
    }

    @Test
    void recommendationsReturnsFallbackWhenNoUser() throws Exception {
        Book book = new Book();
        book.setId(7L);
        book.setTitle("Recommended");
        when(currentUserProvider.maybeCurrentUser()).thenReturn(java.util.Optional.empty());
        when(contentDeliveryService.getRecommendations(null, 10)).thenReturn(List.of(book));

        mockMvc.perform(get("/api/library/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
    }
}
