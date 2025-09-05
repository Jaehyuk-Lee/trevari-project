package com.trevari.project.aop;

import com.trevari.project.api.dto.SearchDTOs;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    @Around("@annotation(MeasureTime)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        Object ret = pjp.proceed();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // ResponseEntity<SearchDTOs.Response> 인 경우
        if (ret instanceof ResponseEntity<?> re) {
            Object body = re.getBody();
            if (body instanceof SearchDTOs.Response r) {
                var newMeta = new SearchDTOs.Metadata(
                        elapsedMs,
                        r.searchMetadata().strategy() // 기존 strategy 유지
                );
                var newBody = new SearchDTOs.Response(
                        r.searchQuery(), r.pageInfo(), r.books(), newMeta
                );
                return ResponseEntity.status(re.getStatusCode())
                        .headers(re.getHeaders())
                        .body(newBody);
            }
        }

        // SearchDTOs.Response 직접 반환하는 경우
        if (ret instanceof SearchDTOs.Response r) {
            var newMeta = new SearchDTOs.Metadata(elapsedMs, r.searchMetadata().strategy());
            return new SearchDTOs.Response(r.searchQuery(), r.pageInfo(), r.books(), newMeta);
        }

        // 대상이 아니면 그대로 반환
        return ret;
    }
}
