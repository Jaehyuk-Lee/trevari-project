package com.trevari.project.repository;

import com.trevari.project.domain.Book;
import com.trevari.project.search.BookSpecifications;
import com.trevari.project.search.SearchQuery;
import com.trevari.project.search.SearchStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositorySliceTest {

    @Autowired
    BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // 1) MongoDB
        bookRepository.save(Book.builder()
                .isbn("9781617291609")
                .title("MongoDB in Action, 2nd Edition")
                .subtitle("Covers MongoDB version 3.0")
                .author("Kyle Banker")
                .publisher("Manning")
                .publishedDate(LocalDate.parse("2016-03-01"))
                .image("https://itbook.store/img/books/9781617291609.png")
                .build());

        // 2) JavaScript
        bookRepository.save(Book.builder()
                .isbn("9780596806750")
                .title("JavaScript Test Patterns")
                .subtitle("Build better applications with coding and design patterns")
                .author("Stoyan Stefanov")
                .publisher("O'Reilly Media")
                .publishedDate(LocalDate.parse("2010-09-09"))
                .image("https://example.com/js-test-patterns.png")
                .build());

        // 3) Test-Driven Development
        bookRepository.save(Book.builder()
                .isbn("9780321146533")
                .title("Test-Driven Development: By Example")
                .author("Kent Beck")
                .publisher("Addison-Wesley")
                .publishedDate(LocalDate.parse("2002-11-08"))
                .image("https://example.com/tdd-by-example.png")
                .build());
    }

    @Test
    @DisplayName("단순 포함 검색: 제목, 저자, 부제목, 출판사 및 ISBN과 일치")
    void simple_contains_matches_title_author_subtitle_publisher_and_isbn() {
        var sq = new SearchQuery("mongodb", "mongodb", null, SearchStrategy.SIMPLE);
        var page = bookRepository.findAll(BookSpecifications.forQuery(sq), PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals("9781617291609", page.getContent().get(0).getIsbn()); // MongoDB in Action
    }

    @Test
    @DisplayName("단순 포함 검색: 대소문자 구분 없음")
    void simple_contains_is_case_insensitive() {
        var sq = new SearchQuery("MONGODB", "MONGODB", null, SearchStrategy.SIMPLE);
        var page = bookRepository.findAll(BookSpecifications.forQuery(sq), PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals("9781617291609", page.getContent().get(0).getIsbn()); // MongoDB in Action
    }

    @Test
    @DisplayName("단순 포함 검색: 검색 결과 없음")
    void tdd_acronym_does_not_match_test_driven_development() {
        var sq = new SearchQuery("tdd", "tdd", null, SearchStrategy.SIMPLE);
        var page = bookRepository.findAll(BookSpecifications.forQuery(sq), PageRequest.of(0, 20));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    @DisplayName("단순 포함 검색: ISBN 일치")
    void simple_contains_can_match_isbn_fragment() {
        var sq = new SearchQuery("1609", "1609", null, SearchStrategy.SIMPLE);
        var page = bookRepository.findAll(BookSpecifications.forQuery(sq), PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals("9781617291609", page.getContent().get(0).getIsbn());
    }

    @Test
    @DisplayName("OR 연산: 리터럴 키워드로 합집합 반환")
    void or_operation_returns_union_by_literal_keywords() {
        // 'test' | 'javascript' => TDD 책 + JS 책 = 2권
        var sq = new SearchQuery("test|javascript", "test", "javascript", SearchStrategy.OR_OPERATION);
        var page = bookRepository.findAll(BookSpecifications.forQuery(sq), PageRequest.of(0, 20));
        assertEquals(2, page.getTotalElements());
        var isbns = page.map(Book::getIsbn).toList();
        assertTrue(isbns.contains("9780321146533")); // Test-Driven Development
        assertTrue(isbns.contains("9780596806750")); // JavaScript Test Patterns
    }

    @Test
    @DisplayName("NOT 연산: 오른쪽 키워드 제외")
    void not_operation_excludes_right_keyword() {
        // 'test' - 'javascript' => TDD 책만 남아야 함 (JS는 제외)
        var sq = new SearchQuery("test-javascript", "test", "javascript", SearchStrategy.NOT_OPERATION);
        var page = bookRepository.findAll(BookSpecifications.forQuery(sq), PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
        assertEquals("9780321146533", page.getContent().get(0).getIsbn()); // Test-Driven Development
    }

    @Test
    @DisplayName("빈 키워드: 분리(disjunction)로 인해 0 반환")
    void empty_keyword_returns_zero_due_to_disjunction() {
        var sq = new SearchQuery("", "", null, SearchStrategy.SIMPLE);
        var page = bookRepository.findAll(BookSpecifications.forQuery(sq), PageRequest.of(0, 20));
        assertEquals(0, page.getTotalElements());
    }
}
