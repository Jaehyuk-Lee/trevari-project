package com.trevari.project.api;

import com.trevari.project.aop.MeasureTime;
import com.trevari.project.api.dto.SearchDTOs;
import com.trevari.project.api.dto.SearchKeyword;
import com.trevari.project.search.SearchQuery;
import com.trevari.project.search.SearchQueryParser;
import com.trevari.project.service.BookService;
import com.trevari.project.service.SearchAggregateService;
import com.trevari.project.service.SearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/api")
@Validated
public class BookController {

    private final SearchService searchService;
    private final BookService bookService;
    private final SearchAggregateService searchAggregateService;

    @GetMapping("/books/{id}")
    public ResponseEntity<SearchDTOs.Book> getBook(@PathVariable("id") String id) {
        return ResponseEntity.ok(bookService.getBookDTO(id));
    }

    @MeasureTime
    @GetMapping("/books") // 리터럴 검색 전용 (SIMPLE 고정)
    public ResponseEntity<SearchDTOs.Response> browse(
        @RequestParam("keyword") String keyword,
        @RequestParam(defaultValue = "1") @Min(1) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        SearchQuery parsed = SearchQueryParser.simple(keyword); // SIMPLE 확정
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(
                searchService.getSearchDTO(parsed, pageable)
        );
    }

    @MeasureTime
    @GetMapping("/search/books") // 연산자 허용 (없으면 SIMPLE)
    public ResponseEntity<SearchDTOs.Response> search(
        @RequestParam("q") String q,
        @RequestParam(defaultValue = "1") @Min(1) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        SearchQuery parsed = SearchQueryParser.parse(q); // OR/NOT/SIMPLE 판정
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(
            searchService.getSearchDTO(parsed, pageable)
        );
    }

    @GetMapping("/analytics/search/top10")
    public ResponseEntity<List<SearchKeyword>> getTop10Keywords() {
        List<SearchKeyword> topKeywords = searchAggregateService.getTop10Keywords();
        return ResponseEntity.ok(topKeywords);
    }
}
