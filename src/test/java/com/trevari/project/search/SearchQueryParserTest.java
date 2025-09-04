package com.trevari.project.search;

import com.trevari.project.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchQueryParserTest {

    @Test
    void parse_simple_keyword() {
        SearchQuery q = SearchQueryParser.parse("  HelloWorld  ");
        assertEquals("HelloWorld", q.original());
        assertEquals("helloworld", q.left());
        assertNull(q.right());
        assertEquals(SearchStrategy.SIMPLE, q.strategy());
    }

    @Test
    void parse_or_operation() {
        SearchQuery q = SearchQueryParser.parse("a|B");
        assertEquals("a|B", q.original());
        assertEquals("a", q.left());
        assertEquals("b", q.right());
        assertEquals(SearchStrategy.OR_OPERATION, q.strategy());
    }

    @Test
    void parse_not_operation() {
        SearchQuery q = SearchQueryParser.parse("tdd-JavaScript");
        assertEquals("tdd-JavaScript", q.original());
        assertEquals("tdd", q.left());
        assertEquals("javascript", q.right());
        assertEquals(SearchStrategy.NOT_OPERATION, q.strategy());
    }

    @Test
    void parse_blank_or_null_throws() {
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse(null));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("   "));
    }

    @Test
    void parse_malformed_or_queries_throw() {
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a|"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("|b"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a|b|c"));
    }

    @Test
    void parse_malformed_not_queries_throw() {
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a-"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("-b"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a-b-c"));
    }

    @Test
    void parse_mixed_operators_throw() {
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a|b-c"));
        assertThrows(BadRequestException.class, () -> SearchQueryParser.parse("a-b|c"));
    }
}
