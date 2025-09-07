package com.trevari.project.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "단건 도서 응답 구조")
public record BookDetailDTO(
        @Schema(description = "식별자 (ISBN과 동일)") String id,
        @Schema(description = "도서 제목") String title,
        @Schema(description = "부제") String subtitle,
        @Schema(description = "표지 이미지 URL") String image,
        @Schema(description = "저자") String author,
        @Schema(description = "ISBN") String isbn,
        @Schema(description = "출판사") String publisher,
        @Schema(description = "출간일 (YYYY-MM-DD)") LocalDate published
) {}
