package com.dlp.service;

import com.dlp.delivery.CdnUrlSigner;
import com.dlp.delivery.DownloadTokenService;
import com.dlp.exception.ContentNotOwnedException;
import com.dlp.model.dto.StreamAccessDTO;
import com.dlp.model.entity.Book;
import com.dlp.model.entity.User;
import com.dlp.model.enums.ContentType;
import com.dlp.recommendation.FallbackRecommendationStrategy;
import com.dlp.recommendation.RecommendationClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class ContentDeliveryService {

    private final CdnUrlSigner cdnUrlSigner;
    private final DownloadTokenService downloadTokenService;
    private final DrmService drmService;
    private final LibraryService libraryService;
    private final BookCatalogService bookCatalogService;
    private final RecommendationClient recommendationClient;
    private final FallbackRecommendationStrategy fallbackStrategy;

    public ContentDeliveryService(CdnUrlSigner cdnUrlSigner,
                                  DownloadTokenService downloadTokenService,
                                  DrmService drmService,
                                  LibraryService libraryService,
                                  BookCatalogService bookCatalogService,
                                  RecommendationClient recommendationClient,
                                  FallbackRecommendationStrategy fallbackStrategy) {
        this.cdnUrlSigner = cdnUrlSigner;
        this.downloadTokenService = downloadTokenService;
        this.drmService = drmService;
        this.libraryService = libraryService;
        this.bookCatalogService = bookCatalogService;
        this.recommendationClient = recommendationClient;
        this.fallbackStrategy = fallbackStrategy;
    }

    public StreamAccessDTO getStreamAccess(User user, ContentType contentType, Long contentId, String deviceFingerprint) {
        ensureAccess(user, contentType, contentId);
        String license = drmService.issueLicense(user, contentId, deviceFingerprint);
        String path = buildResourcePath(contentType, contentId);
        String signed = cdnUrlSigner.signUrl(path, Duration.ofHours(2));
        String token = downloadTokenService.issueToken(user.getId(), contentId, contentType.name(), Duration.ofHours(2));
        return StreamAccessDTO.builder()
                .contentId(String.valueOf(contentId))
                .streamUrl(signed)
                .token(token)
                .expiresInSeconds(Duration.ofHours(2).toSeconds())
                .build();
    }

    public String getDownloadUrl(User user, ContentType contentType, Long contentId) {
        ensureAccess(user, contentType, contentId);
        String path = buildResourcePath(contentType, contentId);
        return cdnUrlSigner.signUrl(path, Duration.ofHours(1));
    }

    public List<Book> getRecommendations(User user, int limit) {
        if (user == null || user.getId() == null) {
            return fallbackStrategy.fallbackPopular(limit);
        }
        try {
            List<Long> ids = recommendationClient.getRecommendationsForUser(user.getId(), limit);
            List<Book> recommended = fallbackStrategy.findByIds(ids);
            return recommended.isEmpty() ? fallbackStrategy.fallbackPopular(limit) : recommended;
        } catch (Exception e) {
            return fallbackStrategy.fallbackPopular(limit);
        }
    }

    private void ensureAccess(User user, ContentType contentType, Long contentId) {
        if (!libraryService.hasAccess(user, contentId, contentType)) {
            throw new ContentNotOwnedException("You do not own this content or an active subscription");
        }
    }

    private String buildResourcePath(ContentType contentType, Long contentId) {
        if (contentType == ContentType.BOOK) {
            Book book = bookCatalogService.getBook(contentId);
            return "content/books/" + contentId + "/" + book.getContentFileUrl();
        }
        return "content/audiobooks/" + contentId + "/stream.m3u8";
    }
}

