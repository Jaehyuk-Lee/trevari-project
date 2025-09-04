package com.trevari.project.search;

import com.trevari.project.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryParserTest {

    @Test
    @DisplayName("단순 키워드 파싱: 공백 제거 및 소문자 정규화")
    void parse_simple_keyword() {
    SearchQuery q = SearchQueryParser.parse("  HelloWorld  ");
    assertEquals("helloworld", q.query());
        assertEquals("helloworld", q.left());
        assertNull(q.right());
        assertEquals(SearchStrategy.SIMPLE, q.strategy());
    }

    @Test
    @DisplayName("OR 연산자 파싱: 'a|b' 형식 처리")
    void parse_or_operation() {
    SearchQuery q = SearchQueryParser.parse("a|B");
    assertEquals("a|b", q.query());
        assertEquals("a", q.left());
        assertEquals("b", q.right());
        assertEquals(SearchStrategy.OR_OPERATION, q.strategy());
    }

    @Test
    @DisplayName("NOT 연산자 파싱: 'a-b' 형식 처리")
    void parse_not_operation() {
    SearchQuery q = SearchQueryParser.parse("tdd-JavaScript");
    assertEquals("tdd-javascript", q.query());
        assertEquals("tdd", q.left());
        assertEquals("javascript", q.right());
        assertEquals(SearchStrategy.NOT_OPERATION, q.strategy());
    }

    @Test
    @DisplayName("빈 문자열 또는 null 입력 시 BadRequestException 발생")
    void parse_blank_or_null_throws() {
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse(null));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("   "));
    }

    @Test
    @DisplayName("잘못된 OR 쿼리 형식은 BadRequestException 발생")
    void parse_malformed_or_queries_throw() {
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a|"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("|b"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a|b|c"));
    }

    @Test
    @DisplayName("잘못된 NOT 쿼리 형식은 BadRequestException 발생")
    void parse_malformed_not_queries_throw() {
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a-"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("-b"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a-b-c"));
    }

    @Test
    @DisplayName("혼합 연산자 사용 시 예외를 던짐")
    void parse_mixed_operators_throw() {
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a|b-c"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a-b|c"));
    }

    @Test
    @DisplayName("연산자 주변 공백 처리: 'a | b'와 동일하게 처리")
    void parse_operator_surrounding_spaces() {
        SearchQuery q1 = SearchQueryParser.parse("a | B");
        assertEquals("a|b", q1.query());
        assertEquals("a", q1.left());
        assertEquals("b", q1.right());

        SearchQuery q2 = SearchQueryParser.parse("tdd - Learn javascript");
        assertEquals("tdd-learn javascript", q2.query());
        assertEquals("tdd", q2.left());
        assertEquals("learn javascript", q2.right());
        assertEquals(SearchStrategy.NOT_OPERATION, q2.strategy());
    }
}
