package com.trevari.project.api.dto;

import com.trevari.project.search.SearchStrategy;

import java.time.LocalDate;
import java.util.List;

/**
 * 도서 검색 응답 DTO 묶음
 */
public final class SearchDTOs {
    private SearchDTOs() {} // 네임스페이스 용, 인스턴스화 방지

    // 상위 응답 DTO
    public record Response(
            String searchQuery,
            PageInfo pageInfo,
            List<Book> books,
            Metadata searchMetadata
    ) {}

    // 페이지 정보
    public record PageInfo(
            int currentPage,
            int pageSize,
            int totalPages,
            long totalElements
    ) {}

    // 도서 정보
    public record Book(
            String id,
            String title,
            String subtitle,
            String image,
            String author,
            String isbn,
            LocalDate published
    ) {}

    // 검색 메타데이터
    public record Metadata(
            long executionTime,
            SearchStrategy strategy
    ) {}
}
