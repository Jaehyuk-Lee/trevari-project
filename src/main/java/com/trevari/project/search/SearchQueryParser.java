package com.trevari.project.search;

import com.trevari.project.exception.BadRequestException;

public class SearchQueryParser {
    // supports: keyword, a|b, a-b (max 2 keywords)
    public static SearchQuery parse(String q) {
        if (q == null || q.trim().isEmpty()) {
            throw new BadRequestException("search query must not be blank");
        }
        String s = q.trim();
        // 전체 문자열 정규화 (공백 제거 + 소문자 변환)
        String normalized = norm(s);
        // 연산자 좌우 공백 제거하는 작업: "a | b" 와 "a|b" 를 동일하게 처리
        String normalizedOps = normalized.replaceAll("\\s*\\|\\s*", "|")
                        .replaceAll("\\s*-\\s*", "-");
        int pipe = normalizedOps.indexOf('|');
        int minus = normalizedOps.indexOf('-');
        if (pipe >= 0 && minus >= 0) {
            throw new BadRequestException("Use only one operator: '|' or '-' with up to 2 keywords");
        }
        if (pipe >= 0) {
            String[] parts = normalizedOps.split("\\|", -1);
            String left = parts[0].trim();
            String right = parts[1].trim();
            if (parts.length != 2 || left.isBlank() || right.isBlank()) {
                throw new BadRequestException("Invalid OR query, use 'a|b'");
            }
            return new SearchQuery(normalizedOps, left, right, SearchStrategy.OR_OPERATION);
        }
        if (minus >= 0) {
            String[] parts = normalizedOps.split("-", -1);
            String left = parts[0].trim();
            String right = parts[1].trim();
            if (parts.length != 2 || left.isBlank() || right.isBlank()) {
                throw new BadRequestException("Invalid NOT query, use 'a-b'");
            }
            return new SearchQuery(normalizedOps, left, right, SearchStrategy.NOT_OPERATION);
        }
        return new SearchQuery(normalizedOps, normalizedOps.trim(), null, SearchStrategy.SIMPLE);
    }

    private static String norm(String kw) { return kw.trim().toLowerCase(); }
}
