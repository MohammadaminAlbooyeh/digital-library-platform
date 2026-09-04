package com.dlp.controller;

import com.dlp.model.dto.ReadingProgressDTO;
import com.dlp.model.entity.User;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.ReadingProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReadingProgressController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class ReadingProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReadingProgressService progressService;

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
    void getProgressReturnsDto() throws Exception {
        ReadingProgressDTO dto = new ReadingProgressDTO();
        dto.setId(1L);
        dto.setContentId(5L);
        dto.setPositionSeconds(360L);
        dto.setTotalSeconds(1200L);
        dto.setProgressPercent(30.0);

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(progressService.getProgress(currentUser, 5L)).thenReturn(dto);

        mockMvc.perform(get("/api/progress/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(5))
                .andExpect(jsonPath("$.positionSeconds").value(360));
    }

    @Test
    void updateProgressReturnsUpdatedDto() throws Exception {
        ReadingProgressDTO input = new ReadingProgressDTO();
        input.setPositionSeconds(600L);

        ReadingProgressDTO saved = new ReadingProgressDTO();
        saved.setId(1L);
        saved.setContentId(5L);
        saved.setPositionSeconds(600L);
        saved.setTotalSeconds(1200L);
        saved.setProgressPercent(50.0);

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(progressService.updateProgress(currentUser, 5L, input)).thenReturn(saved);

        mockMvc.perform(put("/api/progress/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"positionSeconds\":600}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercent").value(50.0));
    }

    @Test
    void updateProgressRejectsMissingPosition() throws Exception {
        mockMvc.perform(put("/api/progress/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }
}
