package com.dlp.controller;

import com.dlp.model.dto.BookDetailDTO;
import com.dlp.model.dto.SearchRequest;
import com.dlp.security.CurrentUserProvider;
import com.dlp.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;
    private final CurrentUserProvider currentUserProvider;

    public SearchController(SearchService searchService, CurrentUserProvider currentUserProvider) {
        this.searchService = searchService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public Page<BookDetailDTO> search(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String author,
                                      @RequestParam(defaultValue = "relevance") String sortBy,
                                      @RequestParam(defaultValue = "desc") String order,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        SearchRequest request = new SearchRequest();
        request.setQuery(q != null ? q : "");
        request.setCategory(category);
        request.setAuthor(author);
        request.setSortBy(sortBy);
        request.setOrder(order);
        request.setPage(page);
        request.setSize(size);

        Long userId = currentUserProvider.maybeCurrentUser().map(u -> u.getId()).orElse(null);
        return searchService.search(request).map(b -> searchService.toDetail(b, userId));
    }
}

