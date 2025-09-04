package com.trevari.project.search;

import com.trevari.project.exception.BadRequestException;

public class SearchQueryParser {
    // supports: keyword, a|b, a-b (max 2 keywords)
    public static SearchQuery parse(String q) {
        if (q == null || q.trim().isEmpty()) {
            throw new BadRequestException("search query must not be blank");
        }
        String s = q.trim();
        int pipe = s.indexOf('|');
        int minus = s.indexOf('-');
        if (pipe >= 0 && minus >= 0) {
            throw new BadRequestException("Use only one operator: '|' or '-' with up to 2 keywords");
        }
        if (pipe >= 0) {
            String[] parts = s.split("\\|", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new BadRequestException("Invalid OR query, use 'a|b'");
            }
            return new SearchQuery(s, norm(parts[0]), norm(parts[1]), SearchStrategy.OR_OPERATION);
        }
        if (minus >= 0) {
            String[] parts = s.split("-", -1);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new BadRequestException("Invalid NOT query, use 'a-b'");
            }
            return new SearchQuery(s, norm(parts[0]), norm(parts[1]), SearchStrategy.NOT_OPERATION);
        }
        return new SearchQuery(s, norm(s), null, SearchStrategy.SIMPLE);
    }

    private static String norm(String kw) { return kw.trim().toLowerCase(); }
}
