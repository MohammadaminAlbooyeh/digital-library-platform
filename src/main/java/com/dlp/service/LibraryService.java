package com.dlp.service;

import com.dlp.model.entity.User;
import com.dlp.model.entity.UserLibraryItem;
import com.dlp.model.enums.AccessType;
import com.dlp.model.enums.ContentType;
import com.dlp.repository.UserLibraryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibraryService {

    private final UserLibraryRepository userLibraryRepository;
    private final SubscriptionService subscriptionService;

    public LibraryService(UserLibraryRepository userLibraryRepository,
                          SubscriptionService subscriptionService) {
        this.userLibraryRepository = userLibraryRepository;
        this.subscriptionService = subscriptionService;
    }

    public List<UserLibraryItem> getLibrary(User user) {
        return userLibraryRepository.findByUserId(user.getId());
    }

    @Transactional
    public UserLibraryItem addToLibrary(User user, Long contentId, ContentType contentType, AccessType accessType) {
        UserLibraryItem item = new UserLibraryItem();
        item.setUser(user);
        item.setContentId(contentId);
        item.setContentType(contentType);
        item.setAccessType(accessType);
        return userLibraryRepository.save(item);
    }

    public boolean hasOwnedAccess(User user, Long contentId, ContentType contentType) {
        return userLibraryRepository.existsByUserIdAndContentIdAndContentType(
                user.getId(), contentId, contentType.name());
    }

    public boolean hasAccess(User user, Long contentId, ContentType contentType) {
        if (hasOwnedAccess(user, contentId, contentType)) {
            return true;
        }
        return contentType == ContentType.BOOK && subscriptionService.hasActiveSubscription(user);
    }
}

