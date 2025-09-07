package com.trevari.project.integration;

import com.trevari.project.api.dto.SearchDTOs;
import com.trevari.project.api.dto.SearchKeyword;
import com.trevari.project.domain.Book;
import com.trevari.project.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class BookE2ETest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    BookRepository bookRepository;

    // Testcontainers Redis (테스트 전용)
    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7").withExposedPorts(6379);

    @BeforeEach
    void setupSeed() {
        bookRepository.deleteAll();

        bookRepository.save(
            Book.builder()
                .isbn("9781617291609")
                .title("Spring in Action - Test Seed")
                .subtitle("Integration Test")
                .author("Craig Walls")
                .publisher("Manning")
                .publishedDate(LocalDate.of(2018, 1, 1))
                .image("")
                .build()
        );
    }

    @Test
    @DisplayName("통합: 단건 조회 API E2E")
    void getBookById() {

        String id = "9781617291609";
        String url = "/api/books/" + id;
        ResponseEntity<SearchDTOs.Book> resp = restTemplate.getForEntity(url, SearchDTOs.Book.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().id()).isNotBlank();
    }

    @Test
    @DisplayName("통합: 단순 키워드 검색 -> Redis 집계 반영 및 인기검색어 확인")
    void searchAggregatesToRedis() throws Exception {
        String keyword = "spring";

        String url = "/api/books?keyword=" + keyword;
        ResponseEntity<SearchDTOs.Response> resp = restTemplate.getForEntity(url, SearchDTOs.Response.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();

        String topUrl = "/api/analytics/search/top10";

        // 비동기 집계가 반영될 때까지 최대 5초 동안 폴링
        boolean found = false;
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(5).toMillis();
        while (System.currentTimeMillis() < deadline) {
            ResponseEntity<SearchKeyword[]> topResp = restTemplate.getForEntity(topUrl, SearchKeyword[].class);
            assertThat(topResp.getStatusCode().is2xxSuccessful()).isTrue();
            SearchKeyword[] body = topResp.getBody();
            if (body != null) {
                found = List.of(body).stream().anyMatch(k -> k.keyword().equalsIgnoreCase(keyword));
                if (found) break;
            }
            Thread.sleep(100);
        }

        assertThat(found).isTrue();
    }
}
