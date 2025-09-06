package com.trevari.project.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// TOP10용 키워드 + 카운트 DTO
@Schema(description = "상위 검색어와 카운트")
public final record SearchKeyword(
	@Schema(description = "검색 키워드") String keyword,
	@Schema(description = "검색 횟수") long count
) {}
