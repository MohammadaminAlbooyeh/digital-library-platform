package com.dlp.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreamAccessDTO {

    private String contentId;
    private String streamUrl;
    private String token;
    private long expiresInSeconds;
}

