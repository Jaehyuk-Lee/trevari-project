package com.trevari.project.api.dto;

import com.trevari.project.search.SearchStrategy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>도서 검색 응답 DTO 묶음:
 * <ul>
 *   <li>Response: 전체 응답 래퍼 (검색어, 페이징정보, 도서 목록, 메타)</li>
 *   <li>PageInfo: 페이징 관련 정보</li>
 *   <li>Book: 단건 도서 응답 구조</li>
 *   <li>Metadata: 검색 메타데이터</li>
 * </ul>
 */
@Schema(description = "도서 검색 응답 DTO 묶음")
public final class SearchDTOs {
    private SearchDTOs() {} // 네임스페이스 용, 인스턴스화 방지

    // 검색 응답 래퍼
    @Schema(description = "검색 응답 래퍼: 검색어, 페이징 정보, 도서 목록, 메타데이터를 포함")
    public record Response(
            @Schema(description = "정규화된 검색 쿼리 문자열") String searchQuery,
            @Schema(description = "페이징 정보") PageInfo pageInfo,
            @Schema(description = "도서 목록") List<Book> books,
            @Schema(description = "검색 실행 메타데이터") Metadata searchMetadata
    ) {}

    @Schema(description = "페이징 관련 정보")
    public record PageInfo(
            @Schema(description = "현재 페이지 번호") int currentPage,
            @Schema(description = "페이지 크기") int pageSize,
            @Schema(description = "전체 페이지 수") int totalPages,
            @Schema(description = "전체 요소 수") long totalElements
    ) {}

    @Schema(description = "검색 결과 도서 정보")
    public record Book(
            @Schema(description = "식별자 (ISBN과 동일)") String id,
            @Schema(description = "도서 제목") String title,
            @Schema(description = "부제") String subtitle,
            @Schema(description = "표지 이미지 URL") String image,
            @Schema(description = "저자") String author,
            @Schema(description = "ISBN") String isbn,
            @Schema(description = "출간일 (YYYY-MM-DD)") LocalDate published
    ) {}

    @Schema(description = "검색 메타데이터")
    public record Metadata(
            @Schema(description = "검색 실행 시간(ms)") long executionTime,
            @Schema(description = "사용한 검색 전략") SearchStrategy strategy
    ) {}
}
