package com.dlp.model.dto;

import lombok.Data;

@Data
public class SearchRequest {

    private String query;
    private String category;
    private String author;
    private String sortBy = "relevance";
    private String order = "desc";
    private int page = 0;
    private int size = 20;
}

