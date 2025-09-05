package com.trevari.project.aop;

import com.trevari.project.api.dto.SearchDTOs;
import com.trevari.project.search.SearchStrategy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExecutionTimeAspectTest {

    @InjectMocks
    ExecutionTimeAspect aspect;

    @Test
    @DisplayName("단순 ResponseEntity 래핑: executionTime 삽입 검증")
    void wrapsResponseEntity_andInjectsExecutionTime() throws Throwable {
        // given: 기존 응답(메타 executionTime=0)
        var meta = new SearchDTOs.Metadata(0L, SearchStrategy.SIMPLE);
        var body = new SearchDTOs.Response("mongodb",
                new SearchDTOs.PageInfo(1,20,0,0),
                List.of(), meta);
        var original = ResponseEntity.ok(body);

        // and: pjp.proceed()가 그 응답을 돌려줌
        ProceedingJoinPoint pjp = Mockito.mock(ProceedingJoinPoint.class);
        Mockito.when(pjp.proceed()).thenAnswer(invocation -> {
            Thread.sleep(2); // 2ms 보장
            return original;
        });

        // when
        Object ret = aspect.around(pjp);

        // then
        assertNotNull(ret);
        assertInstanceOf(ResponseEntity.class, ret, "returned object should be a ResponseEntity");
        ResponseEntity<?> re = (ResponseEntity<?>) ret;
        var changed = (SearchDTOs.Response) re.getBody();
        assertNotNull(changed);
        assertEquals(SearchStrategy.SIMPLE, changed.searchMetadata().strategy());
        assertNotEquals(0L, changed.searchMetadata().executionTime()); // 실행 시간이 초기값과 달라졌음을 검증
    }
}
