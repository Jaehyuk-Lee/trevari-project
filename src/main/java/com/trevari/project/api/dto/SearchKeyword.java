package com.trevari.project.api.dto;

// TOP10용 키워드 + 카운트 DTO
public final record SearchKeyword(String keyword, long count) {}
