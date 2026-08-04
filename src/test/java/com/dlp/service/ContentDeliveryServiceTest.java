package com.dlp.service;

import com.dlp.exception.ContentNotOwnedException;
import com.dlp.model.dto.StreamAccessDTO;
import com.dlp.model.entity.Book;
import com.dlp.model.entity.User;
import com.dlp.model.enums.ContentType;
import com.dlp.recommendation.FallbackRecommendationStrategy;
import com.dlp.recommendation.RecommendationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ContentDeliveryServiceTest {

    private ContentDeliveryService service;
    private LibraryService libraryService;
    private DrmService drmService;
    private BookCatalogService catalogService;

    @BeforeEach
    void setUp() {
        libraryService = mock(LibraryService.class);
        drmService = mock(DrmService.class);
        catalogService = mock(BookCatalogService.class);
        var cdnUrlSigner = new com.dlp.delivery.CdnUrlSigner();
        ReflectionTestUtils.setField(cdnUrlSigner, "cdnBaseUrl", "https://cdn.example.com");
        ReflectionTestUtils.setField(cdnUrlSigner, "signingKey", "key");
        var downloadTokenService = mock(com.dlp.delivery.DownloadTokenService.class);
        when(downloadTokenService.issueToken(anyLong(), anyLong(), anyString(), any())).thenReturn("tok");

        service = new ContentDeliveryService(
                cdnUrlSigner, downloadTokenService, drmService, libraryService, catalogService,
                mock(RecommendationClient.class), mock(FallbackRecommendationStrategy.class));
    }

    @Test
    void streamAccessRequiresOwnership() {
        when(libraryService.hasAccess(any(), anyLong(), any())).thenReturn(false);
        User user = new User();
        user.setId(1L);
        assertThrows(ContentNotOwnedException.class,
                () -> service.getStreamAccess(user, ContentType.BOOK, 7L, "fp"));
    }

    @Test
    void streamAccessReturnsSignedUrlWhenOwned() {
        when(libraryService.hasAccess(any(), anyLong(), any())).thenReturn(true);
        when(drmService.issueLicense(any(), anyLong(), anyString())).thenReturn("license");
        Book book = new Book();
        book.setContentFileUrl("file.epub");
        when(catalogService.getBook(7L)).thenReturn(book);

        User user = new User();
        user.setId(1L);
        StreamAccessDTO dto = service.getStreamAccess(user, ContentType.BOOK, 7L, "fp");
        assertNotNull(dto);
        assertTrue(dto.getStreamUrl().startsWith("https://cdn.example.com/content/books/7/"));
        assertTrue(dto.getStreamUrl().contains("signature="));
        assertNotNull(dto.getToken());
    }

    @Test
    void recommendationsFallbackWorks() {
        var fallback = mock(FallbackRecommendationStrategy.class);
        Book b = new Book();
        b.setId(1L);
        when(fallback.fallbackPopular(5)).thenReturn(List.of(b));
        // Use a service with fallback wired directly
        ContentDeliveryService s2 = new ContentDeliveryService(
                mock(com.dlp.delivery.CdnUrlSigner.class),
                mock(com.dlp.delivery.DownloadTokenService.class),
                mock(DrmService.class), mock(LibraryService.class), mock(BookCatalogService.class),
                mock(RecommendationClient.class), fallback);
        var recs = s2.getRecommendations(null, 5);
        assertEquals(1, recs.size());
        assertEquals(1L, recs.get(0).getId());
    }
}

