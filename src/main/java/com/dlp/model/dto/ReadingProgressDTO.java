package com.dlp.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReadingProgressDTO {

    private Long id;
    private Long contentId;

    @NotNull
    private Long positionSeconds;

    private Long totalSeconds;
    private Double progressPercent;
}

