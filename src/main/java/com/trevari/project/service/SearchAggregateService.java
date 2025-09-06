package com.trevari.project.service;

import com.trevari.project.api.dto.SearchKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class SearchAggregateService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SEARCH_QUERY_KEY = "search:query:popular";

    // 검색어를 집계하여 인기 검색어 순위에 반영 (비동기 처리)
    @Async("searchAggregateExecutor")
    public CompletableFuture<Void> aggregateTop10(String query) {
        if (query == null || (query = query.trim()).isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            zSetOps.incrementScore(SEARCH_QUERY_KEY, query, 1.0);

            log.debug("검색어 집계 완료: {} (스레드: {})", query, Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("검색어 집계 실패: query={}", query, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    // 인기 검색어 TOP10 및 조회수 반환
    public List<SearchKeyword> getTop10Keywords() {
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            Set<ZSetOperations.TypedTuple<String>> topWithScores = zSetOps.reverseRangeWithScores(SEARCH_QUERY_KEY, 0, 9);

            return Optional.ofNullable(topWithScores)
                    .orElseGet(Set::of) // null이면 빈 Set 반환
                    .stream()
                    .map(t -> {
                        String kw = t.getValue();
                        Double score = t.getScore();
                        long cnt = score == null ? 0L : Math.round(score);
                        return new SearchKeyword(kw, cnt);
                    })
                    .toList();


        } catch (Exception e) {
            log.error("인기 검색어 조회 실패", e);
            return List.of();
        }
    }
}
