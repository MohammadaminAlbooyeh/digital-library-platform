package com.dlp.service;

import com.dlp.messaging.ReadingEventProducer;
import com.dlp.model.dto.ReadingProgressDTO;
import com.dlp.model.entity.ReadingProgress;
import com.dlp.model.entity.User;
import com.dlp.repository.ReadingProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingProgressServiceTest {

    @Mock private ReadingProgressRepository progressRepository;
    @Mock private ReadingEventProducer readingEventProducer;

    private ReadingProgressService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new ReadingProgressService(progressRepository, readingEventProducer);
        user = new User();
        user.setId(1L);
    }

    @Test
    void updateComputesPercentWhenNotSupplied() {
        when(progressRepository.findByUserIdAndContentId(1L, 2L)).thenReturn(Optional.empty());
        when(progressRepository.save(any(ReadingProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        ReadingProgressDTO dto = new ReadingProgressDTO();
        dto.setPositionSeconds(30L);
        dto.setTotalSeconds(120L);

        ReadingProgressDTO result = service.updateProgress(user, 2L, dto);

        assertEquals(25.0, result.getProgressPercent());
        verify(readingEventProducer).publishReadingProgress(1L, 2L, 30L, 25.0);
    }

    @Test
    void updateHonoursExplicitPercentAndCapsComputedAt100() {
        when(progressRepository.findByUserIdAndContentId(1L, 2L)).thenReturn(Optional.empty());
        when(progressRepository.save(any(ReadingProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        ReadingProgressDTO dto = new ReadingProgressDTO();
        dto.setPositionSeconds(500L);
        dto.setTotalSeconds(100L);
        dto.setProgressPercent(42.0);

        assertEquals(42.0, service.updateProgress(user, 2L, dto).getProgressPercent());
    }

    @Test
    void updateReusesExistingRow() {
        ReadingProgress existing = new ReadingProgress();
        existing.setId(99L);
        existing.setUser(user);
        existing.setContentId(2L);
        when(progressRepository.findByUserIdAndContentId(1L, 2L)).thenReturn(Optional.of(existing));
        when(progressRepository.save(any(ReadingProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        ReadingProgressDTO dto = new ReadingProgressDTO();
        dto.setPositionSeconds(10L);

        assertEquals(99L, service.updateProgress(user, 2L, dto).getId());
    }

    @Test
    void getProgressThrowsWhenMissing() {
        when(progressRepository.findByUserIdAndContentId(1L, 8L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getProgress(user, 8L));
    }
}
