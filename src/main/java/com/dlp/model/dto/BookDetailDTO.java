package com.dlp.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class BookDetailDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer pageCount;
    private String isbn;
    private String coverImageUrl;
    private String format;
    private Integer publishedYear;
    private Set<String> authors;
    private Set<String> categories;
    private boolean owned;
    private boolean subscribed;
}

