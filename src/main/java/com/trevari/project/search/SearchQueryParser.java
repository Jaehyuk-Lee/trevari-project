package com.trevari.project.search;

import com.trevari.project.exception.BadRequestException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class SearchQueryParser {

    private SearchQueryParser() {}

    // 공백-연산자-공백을 단일 기호로 정규화하기 위한 패턴 (재사용)
    private static final Pattern WS_AROUND_PIPE  = Pattern.compile("\\s*\\|\\s*");
    private static final Pattern WS_AROUND_MINUS = Pattern.compile("\\s*-\\s*");

    /** 단순 검색: 리터럴 그대로 SIMPLE로 */
    public static SearchQuery simple(String q) {
        return parseInternal(q, false);
    }

    /** 연산자 허용: OR('|'), NOT('-'), 없으면 SIMPLE */
    public static SearchQuery parse(String q) {
        return parseInternal(q, true);
    }

    /** 내부 단일 진입점: 항상 정규화 선행 + 분기 */
    private static SearchQuery parseInternal(String q, boolean allowOperators) {
        String normalized = normalize(q);              // 1) trim + toLowerCase
        if (!allowOperators) {
            // 연산자 해석 없이 그대로 SIMPLE
            return new SearchQuery(normalized, normalized, null, SearchStrategy.SIMPLE);
        }

        // 2) 연산자 좌우 공백 정규화
        String ops = normalizeOperators(normalized);

        // 3) 연산자 판별 및 검증
        int pipe  = ops.indexOf('|');
        int minus = ops.indexOf('-');

        if (pipe >= 0 && minus >= 0) {
            throw new BadRequestException("Use only one operator: '|' or '-' with up to 2 keywords");
        }

        if (pipe >= 0) {
            String[] parts = ops.split("\\|", -1);
            if (parts.length != 2) throw new BadRequestException("Invalid OR query, use 'a|b'");
            String left = parts[0].trim();
            String right = parts[1].trim();
            if (left.isBlank() || right.isBlank()) throw new BadRequestException("Invalid OR query, use 'a|b'");
            return new SearchQuery(ops, left, right, SearchStrategy.OR_OPERATION);
        }

        if (minus >= 0) {
            String[] parts = ops.split("-", -1);
            if (parts.length != 2) throw new BadRequestException("Invalid NOT query, use 'a-b'");
            String left = parts[0].trim();
            String right = parts[1].trim();
            if (left.isBlank() || right.isBlank()) throw new BadRequestException("Invalid NOT query, use 'a-b'");
            return new SearchQuery(ops, left, right, SearchStrategy.NOT_OPERATION);
        }

        // 연산자 없음 → SIMPLE
        return new SearchQuery(normalized, normalized, null, SearchStrategy.SIMPLE);
    }

    /** 입력 문자열 공통 정규화: trim → lower(Locale.ROOT) */
    private static String normalize(String q) {
        if (q == null) throw new BadRequestException("search query must not be null");
        q = q.trim();
        if (q.isEmpty()) throw new BadRequestException("search query must not be blank");
        return q.toLowerCase(Locale.ROOT);
    }

    /** 연산자 주변 공백 제거 "a | b" → "a|b", "a - b" → "a-b" */
    private static String normalizeOperators(String s) {
        return WS_AROUND_MINUS.matcher(WS_AROUND_PIPE.matcher(s).replaceAll("|")).replaceAll("-");
    }
}
