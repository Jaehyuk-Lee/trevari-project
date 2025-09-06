package com.trevari.project.service;

import com.trevari.project.api.dto.SearchDTOs;
import com.trevari.project.domain.Book;
import com.trevari.project.repository.BookRepository;
import com.trevari.project.search.BookSpecifications;
import com.trevari.project.search.SearchQuery;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class SearchService {
    private final BookRepository bookRepository;
    private final SearchAggregateService searchAggregateService;

    @Transactional(readOnly = true)
    public SearchDTOs.Response getSearchDTO(SearchQuery searchQuery, Pageable pageable) {
        // 검색어 집계 (인기 검색어 TOP10)
        searchAggregateService.aggregateTop10(searchQuery.query());
        
        Page<Book> pageData = bookRepository.findAll(BookSpecifications.forQuery(searchQuery), pageable);
        var items = pageData.getContent().stream()
            .map(b -> new SearchDTOs.Book(
                b.getIsbn(), b.getTitle(), b.getSubtitle(), b.getImage(),
                b.getAuthor(), b.getIsbn(), b.getPublishedDate()
            ))
            .toList();

        var pageInfo = new SearchDTOs.PageInfo(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageData.getTotalPages(),
                pageData.getTotalElements()
        );

        var metadata = new SearchDTOs.Metadata(
            0L, searchQuery.strategy()
        );

        return new SearchDTOs.Response(
            searchQuery.query(), pageInfo, items, metadata
        );
    }
}
