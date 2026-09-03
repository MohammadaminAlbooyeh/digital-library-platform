package com.dlp.service;

import com.dlp.model.entity.User;
import com.dlp.model.entity.UserLibraryItem;
import com.dlp.model.enums.AccessType;
import com.dlp.model.enums.ContentType;
import com.dlp.repository.UserLibraryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock private UserLibraryRepository userLibraryRepository;
    @Mock private SubscriptionService subscriptionService;

    private LibraryService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new LibraryService(userLibraryRepository, subscriptionService);
        user = new User();
        user.setId(1L);
    }

    @Test
    void addToLibrarySetsFields() {
        when(userLibraryRepository.save(any(UserLibraryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        UserLibraryItem item = service.addToLibrary(user, 4L, ContentType.BOOK, AccessType.OWNED);

        assertEquals(4L, item.getContentId());
        assertEquals(ContentType.BOOK, item.getContentType());
        assertEquals(AccessType.OWNED, item.getAccessType());
        assertSame(user, item.getUser());
    }

    @Test
    void hasAccessTrueWhenOwned() {
        when(userLibraryRepository.existsByUserIdAndContentIdAndContentType(1L, 4L, "BOOK"))
                .thenReturn(true);

        assertTrue(service.hasAccess(user, 4L, ContentType.BOOK));
        verifyNoInteractions(subscriptionService);
    }

    @Test
    void hasAccessFallsBackToSubscriptionForBooks() {
        when(userLibraryRepository.existsByUserIdAndContentIdAndContentType(1L, 4L, "BOOK"))
                .thenReturn(false);
        when(subscriptionService.hasActiveSubscription(user)).thenReturn(true);

        assertTrue(service.hasAccess(user, 4L, ContentType.BOOK));
    }

    @Test
    void hasAccessDeniedForAudiobookWithoutOwnership() {
        when(userLibraryRepository.existsByUserIdAndContentIdAndContentType(1L, 9L, "AUDIOBOOK"))
                .thenReturn(false);

        assertFalse(service.hasAccess(user, 9L, ContentType.AUDIOBOOK));
        verifyNoInteractions(subscriptionService);
    }
}
