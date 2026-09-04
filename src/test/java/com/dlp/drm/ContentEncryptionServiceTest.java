package com.dlp.drm;

import com.dlp.kms.KmsDataKeyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ContentEncryptionServiceTest {

    private ContentEncryptionService service;

    private ContentEncryptionService service() {
        if (service == null) {
            KmsDataKeyService kms = Mockito.mock(KmsDataKeyService.class);
            service = new ContentEncryptionService("0123456789abcdef", kms);
        }
        return service;
    }

    @Test
    void encryptDecryptRoundtripPreservesContent() throws Exception {
        ContentEncryptionService svc = service();
        String original = "license-payload-data";

        String encrypted = svc.encryptToBase64(original);
        String decrypted = svc.decryptFromBase64(encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    void eachEncryptionUsesDifferentIV() throws Exception {
        ContentEncryptionService svc = service();
        String plaintext = "same-license";

        String enc1 = svc.encryptToBase64(plaintext);
        String enc2 = svc.encryptToBase64(plaintext);

        assertNotEquals(enc1, enc2, "Ciphertext should differ due to random IV");

        assertEquals(plaintext, svc.decryptFromBase64(enc1));
        assertEquals(plaintext, svc.decryptFromBase64(enc2));
    }

    @Test
    void decryptCorruptedCiphertextThrows() {
        ContentEncryptionService svc = service();
        String corrupted = "invalid-base64-ciphertext";

        assertThrows(Exception.class, () -> svc.decryptFromBase64(corrupted));
    }
}
