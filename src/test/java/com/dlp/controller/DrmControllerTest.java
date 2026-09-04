package com.dlp.controller;

import com.dlp.model.entity.Device;
import com.dlp.model.entity.User;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.DrmService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DrmController.class)
@Import(com.dlp.config.TestSecurityConfig.class)
class DrmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DrmService drmService;

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

    @Test
    void registerDeviceReturnsCreated() throws Exception {
        Device device = new Device();
        device.setId(10L);
        device.setDeviceName("Kindle");
        device.setDeviceType("reader");
        device.setDeviceFingerprint("fp-001");
        device.setRegistered(true);

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(drmService.registerDevice(currentUser, "Kindle", "reader", "fp-001"))
                .thenReturn(device);

        mockMvc.perform(post("/api/drm/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceName\":\"Kindle\",\"deviceType\":\"reader\",\"deviceFingerprint\":\"fp-001\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.deviceName").value("Kindle"));
    }

    @Test
    void devicesReturnsList() throws Exception {
        Device device = new Device();
        device.setId(10L);
        device.setDeviceName("Kindle");
        device.setDeviceFingerprint("fp-001");
        device.setRegistered(true);

        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        when(drmService.listDevices(currentUser)).thenReturn(List.of(device));

        mockMvc.perform(get("/api/drm/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceName").value("Kindle"));
    }

    @Test
    void unregisterReturnsNoContent() throws Exception {
        when(currentUserProvider.currentUser()).thenReturn(currentUser);
        org.mockito.Mockito.doNothing()
                .when(drmService).unregisterDevice(currentUser, "fp-001");

        mockMvc.perform(delete("/api/drm/devices")
                        .param("deviceFingerprint", "fp-001"))
                .andExpect(status().isNoContent());
    }
}
