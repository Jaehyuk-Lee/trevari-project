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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * REST API 진입점: 도서 조회 및 검색 관련 엔드포인트를 제공합니다.
 *
 * <p>주요 엔드포인트:
 * <ul>
 *   <li>GET  /api/books/{id}            : ID로 단건 도서 조회</li>
 *   <li>GET  /api/books                 : 단순 키워드(SIMPLE)로 페이징 검색</li>
 *   <li>GET  /api/search/books          : 고급 검색(OR/NOT 등 연산자 허용)</li>
 *   <li>GET  /api/analytics/search/top10: 인기 검색어 TOP10 조회</li>
 * </ul>
 */
@RestController
@Tag(name = "Book API", description = "책 조회 및 검색 관련 API")
@AllArgsConstructor
@RequestMapping("/api")
@Validated
public class BookController {

    private final SearchService searchService;
    private final BookService bookService;
    private final SearchAggregateService searchAggregateService;

    /**
     * 단건 도서 조회
     *
     * @param id 도서 식별자
     * @return 도서 응답 DTO
     */
    @GetMapping("/books/{id}")
    @Operation(summary = "책 단건 조회", description = "ID로 책 상세 정보를 조회합니다.")
    public ResponseEntity<SearchDTOs.Book> getBook(@PathVariable("id") String id) {
        return ResponseEntity.ok(bookService.getBookDTO(id));
    }

    /**
     * 단순 키워드로 도서를 조회합니다. (SIMPLE 검색 모드 고정)
     *
     * @param keyword 검색 키워드 (리터럴)
     * @param page 1 기반 페이지 번호
     * @param size 페이지 크기 (최대 100)
     * @return 페이징된 검색 결과
     */
    @MeasureTime
    @GetMapping("/books") // 리터럴 검색 전용 (SIMPLE 고정)
    @Operation(summary = "리터럴 키워드로 책 목록 조회", description = "단순 키워드로 책을 검색합니다. 페이징 지원")
    public ResponseEntity<SearchDTOs.Response> browse(
        @Parameter(description = "검색 키워드 (단순 텍스트)") @RequestParam("keyword") String keyword,
        @Parameter(description = "페이지 번호 (1 기반)") @RequestParam(defaultValue = "1") @Min(1) int page,
        @Parameter(description = "페이지 사이즈") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        SearchQuery parsed = SearchQueryParser.simple(keyword); // SIMPLE 확정
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(
                searchService.getSearchDTO(parsed, pageable)
        );
    }

    /**
     * 고급 검색: OR/NOT 등의 연산자를 포함한 쿼리를 파싱하여 검색을 수행합니다.
     *
     * @param q 검색 쿼리 문자열
     * @param page 1 기반 페이지 번호
     * @param size 페이지 크기 (최대 100)
     * @return 페이징된 검색 결과
     */
    @MeasureTime
    @GetMapping("/search/books") // 연산자 허용 (없으면 SIMPLE)
    @Operation(summary = "고급 검색", description = "OR/NOT 등의 연산자를 포함한 고급 검색을 수행합니다.")
    public ResponseEntity<SearchDTOs.Response> search(
        @Parameter(description = "검색 쿼리 (OR/NOT 등 사용 가능)") @RequestParam("q") String q,
        @Parameter(description = "페이지 번호 (1 기반)") @RequestParam(defaultValue = "1") @Min(1) int page,
        @Parameter(description = "페이지 사이즈") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        SearchQuery parsed = SearchQueryParser.parse(q); // OR/NOT/SIMPLE 판정
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(
            searchService.getSearchDTO(parsed, pageable)
        );
    }

    /**
     * 인기 검색어 TOP10을 반환합니다.
     *
     * <p>이 엔드포인트는 검색 집계 서비스에서 집계된 결과를 반환하며, UI나 리포트에서
     * 상위 검색어를 표시할 때 사용됩니다.
     *
     * @return 상위 10개 검색어 리스트
     */
    @GetMapping("/analytics/search/top10")
    @Operation(summary = "상위 10 검색어 조회", description = "가장 많이 검색된 상위 10개 키워드를 반환합니다.")
    public ResponseEntity<List<SearchKeyword>> getTop10Keywords() {
        List<SearchKeyword> topKeywords = searchAggregateService.getTop10Keywords();
        return ResponseEntity.ok(topKeywords);
    }
}
