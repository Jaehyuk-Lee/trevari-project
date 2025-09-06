package com.trevari.project.service;

import com.trevari.project.api.dto.SearchKeyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
@Testcontainers
@Import({SearchAggregateService.class, SearchAggregateServiceSliceTest.AsyncTestConfig.class})
class SearchAggregateServiceSliceTest {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    SearchAggregateService searchAggregateService;

    // Testcontainers Redis (테스트 전용)
    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7").withExposedPorts(6379);

    @BeforeEach
    // 테스트 메서드 2개 이상일 경우, 이전 테스트의 부작용이 남지 않도록 처리
    void setUp() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    @DisplayName("단순 집계 통합: 검색어 집계 후 TOP10에 반영됨")
    void aggregate_simple_keyword_and_get_top10() {
        // Given: 키워드별 호출 횟수 정의
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("spring boot", 2);
        counts.put("mongo-java", 5);
        counts.put("tdd-javascript", 3);

        // When: Map을 이용해 반복적으로 비동기 집계 호출
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            String k = e.getKey();
            int times = e.getValue();
            for (int i = 0; i < times; i++) {
                futures.add(searchAggregateService.aggregateTop10(k));
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Then: TOP 결과 확인 (내림차순으로 정렬되어야 함)
        List<SearchKeyword> top = searchAggregateService.getTop10Keywords();
        assertThat(top).isNotEmpty();

        // 카운트 맵 값 기반 예상 순서: mongo-java(5), tdd-javascript(3), spring boot(2)
        Map<String, Long> expected = new LinkedHashMap<>();
        expected.put("mongo-java", 5L);
        expected.put("tdd-javascript", 3L);
        expected.put("spring boot", 2L);

        int idx = 0;
        for (Map.Entry<String, Long> e : expected.entrySet()) {
            assertThat(top.get(idx).keyword()).isEqualTo(e.getKey());
            assertThat(top.get(idx).count()).isEqualTo(e.getValue());
            idx++;
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = RedisRepositoriesAutoConfiguration.class)
    @EnableAsync
    static class AsyncTestConfig {
        @Bean(name = "searchAggregateExecutor")
        public Executor searchAggregateExecutor() {
            ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
            exec.setCorePoolSize(2);
            exec.setMaxPoolSize(4);
            exec.setQueueCapacity(50);
            exec.setThreadNamePrefix("test-search-agg-");
            exec.initialize();
            return exec;
        }
    }
}
