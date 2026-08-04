package com.dlp.drm;

import com.dlp.exception.DrmViolationException;
import com.dlp.model.entity.Device;
import com.dlp.model.entity.User;
import com.dlp.repository.DeviceRepository;
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
class DeviceRegistrationServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    private DeviceRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new DeviceRegistrationService(deviceRepository);
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
        return user;
    }

    @Test
    void registerNewDeviceSavesDevice() {
        when(deviceRepository.findByDeviceFingerprint(any())).thenReturn(Optional.empty());
        when(deviceRepository.countByUserId(1L)).thenReturn(0L);
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        Device device = service.registerDevice(user(), "Kindle", "reader", "fp-1");
        assertNotNull(device);
        assertTrue(device.isRegistered());
        assertTrue(device.getDeviceFingerprint().length() == 64);
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    void registerExistingDeviceReusesIt() {
        Device existing = new Device();
        existing.setId(5L);
        existing.setRegistered(true);
        when(deviceRepository.findByDeviceFingerprint(any())).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenReturn(existing);

        Device device = service.registerDevice(user(), "Kindle", "reader", "fp-1");
        assertEquals(5L, device.getId());
        verify(deviceRepository, never()).countByUserId(anyLong());
    }

    @Test
    void registerBeyondMaxDevicesThrows() {
        when(deviceRepository.findByDeviceFingerprint(any())).thenReturn(Optional.empty());
        when(deviceRepository.countByUserId(1L)).thenReturn(5L);
        assertThrows(DrmViolationException.class,
                () -> service.registerDevice(user(), "Kindle", "reader", "fp-new"));
    }
}

