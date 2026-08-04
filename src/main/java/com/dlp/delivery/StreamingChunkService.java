package com.dlp.delivery;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StreamingChunkService {

    public List<byte[]> splitIntoChunks(byte[] content, int chunkSize) {
        List<byte[]> chunks = new ArrayList<>();
        int offset = 0;
        while (offset < content.length) {
            int length = Math.min(chunkSize, content.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(content, offset, chunk, 0, length);
            chunks.add(chunk);
            offset += length;
        }
        return chunks;
    }

    public byte[] reassemble(List<byte[]> chunks) {
        int total = 0;
        for (byte[] c : chunks) {
            total += c.length;
        }
        byte[] result = new byte[total];
        int offset = 0;
        for (byte[] c : chunks) {
            System.arraycopy(c, 0, result, offset, c.length);
            offset += c.length;
        }
        return result;
    }

    public int defaultChunkSize() {
        return 1024 * 1024;
    }
}

