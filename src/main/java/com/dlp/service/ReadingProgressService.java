package com.dlp.service;

import com.dlp.messaging.ReadingEventProducer;
import com.dlp.model.dto.ReadingProgressDTO;
import com.dlp.model.entity.ReadingProgress;
import com.dlp.model.entity.User;
import com.dlp.repository.ReadingProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingProgressService {

    private final ReadingProgressRepository progressRepository;
    private final ReadingEventProducer readingEventProducer;

    public ReadingProgressService(ReadingProgressRepository progressRepository,
                                  ReadingEventProducer readingEventProducer) {
        this.progressRepository = progressRepository;
        this.readingEventProducer = readingEventProducer;
    }

    @Transactional
    public ReadingProgressDTO updateProgress(User user, Long contentId, ReadingProgressDTO dto) {
        ReadingProgress progress = progressRepository.findByUserIdAndContentId(user.getId(), contentId)
                .orElseGet(() -> {
                    ReadingProgress p = new ReadingProgress();
                    p.setUser(user);
                    p.setContentId(contentId);
                    return p;
                });
        progress.setPositionSeconds(dto.getPositionSeconds());
        if (dto.getTotalSeconds() != null) {
            progress.setTotalSeconds(dto.getTotalSeconds());
        }
        double percent = dto.getProgressPercent() != null
                ? dto.getProgressPercent()
                : computePercent(progress.getPositionSeconds(), progress.getTotalSeconds());
        progress.setProgressPercent(percent);
        ReadingProgress saved = progressRepository.save(progress);

        readingEventProducer.publishReadingProgress(user.getId(), contentId,
                saved.getPositionSeconds(), saved.getProgressPercent());

        return toDto(saved);
    }

    public ReadingProgressDTO getProgress(User user, Long contentId) {
        return progressRepository.findByUserIdAndContentId(user.getId(), contentId)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("No progress recorded for content " + contentId));
    }

    private double computePercent(Long position, Long total) {
        if (total == null || total <= 0 || position == null) {
            return 0.0;
        }
        return Math.min(100.0, (position * 100.0) / total);
    }

    private ReadingProgressDTO toDto(ReadingProgress p) {
        ReadingProgressDTO dto = new ReadingProgressDTO();
        dto.setId(p.getId());
        dto.setContentId(p.getContentId());
        dto.setPositionSeconds(p.getPositionSeconds());
        dto.setTotalSeconds(p.getTotalSeconds());
        dto.setProgressPercent(p.getProgressPercent());
        return dto;
    }
}

