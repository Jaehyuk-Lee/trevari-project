# 도메인 모델

## 1) 도메인 개요

이 시스템은 **도서(Book) 카탈로그**를 중심으로,

* 사용자의 **도서 검색**(단순/복합 연산)
* **도서 상세 조회**
* **인기 검색어 집계/조회**

를 제공하는 **검색 중심 도메인**입니다. API 응답에는 검색 실행 메타데이터(실행 시간, 전략 등)가 포함됩니다.

도메인은 크게 세 하위 영역으로 나뉩니다.

* **Catalog**: 도서 엔티티의 저장/조회 (RDBMS, JPA)
* **Search**: 검색 쿼리 파싱, Specification 기반 검색 (SIMPLE / OR / NOT)
* **Analytics**: 검색어 집계 및 TOP10 조회 (Redis ZSET, 비동기 집계)

## 2) 도메인 객체

### 2.1 Book (Entity)

* 위치: `com.trevari.project.domain.Book`
* 저장소: RDBMS (`books` 테이블)
* **식별자**: `isbn` (String) — Equals/HashCode는 isbn 기준
* **주요 속성**
  * `title` (String, not null)
  * `subtitle` (String)
  * `author` (String, not null)
  * `publisher` (String)
  * `publishedDate` (LocalDate, 컬럼명 `published_date`)
  * `image` (String, 표지 이미지 URL)
* **불변 조건/관례**
  * `isbn`, `title`, `author`는 비어선 안 됨(NotNull)
  * API 단에서는 `id == isbn`으로 동일하게 취급

### 2.2 SearchQuery (Value Object)

* 위치: `com.trevari.project.search.SearchQuery`
* 필드: `query`, `left`, `right`, `strategy`
* **의미**
  * `SIMPLE`: 단일 키워드 검색
  * `OR`: `left | right`
  * `NOT`: `left - right`
* 생성은 **파서**를 통해 일관성 있게 이뤄집니다.

### 2.3 SearchStrategy (Enum)

* 위치: `com.trevari.project.search.SearchStrategy`
* 값: `SIMPLE`, `OR`, `NOT`
* **행위적 의미**: Book 검색 시 어떤 조합의 `Specification`을 만들지 결정

## 3) 도메인 서비스 / 응용 서비스

### 3.1 BookService (응용 서비스)

* 위치: `com.trevari.project.service.BookService`
* 역할: 단건 상세 조회 흐름 조립

  * `getBookDetailDTO(id)` → `BookRepository.findById` → DTO 매핑
  * 존재하지 않을 경우 `NotFoundException`

### 3.2 SearchService (응용 서비스)

* 위치: `com.trevari.project.service.SearchService`
* 역할: 검색 흐름 조립

  * `getSearchDTO(SearchQuery, Pageable)`
  * `BookSpecifications.forQuery(searchQuery)`를 통해 `Specification<Book>` 생성
  * Page 결과를 `SearchDTOs`로 매핑 (목록/페이징/메타데이터)

### 3.3 SearchAggregateService (도메인/응용 혼합, Analytics)

* 위치: `com.trevari.project.service.SearchAggregateService`
* 역할:

  * **검색어 집계(비동기)**: `aggregateTop10(query)`
    * Redis ZSET(`search:query:popular`)에 `query`의 score 증가
    * `@Async("searchAggregateExecutor")`로 처리 (최소한의 쓰기 지연)
  * **TOP10 조회**: `getTop10()`

    * ZSET에서 상위 10개 `(keyword, count)` 가공 후 반환
* 일관성 모델: **최종 일관성(Eventual Consistency)**
  검색 직후 TOP10에 바로 반영되지 않을 수 있음

## 4) 규칙과 제약 (도메인 정책)

### 4.1 검색 쿼리 파싱 규칙

* 위치: `com.trevari.project.search.SearchQueryParser`
* 입력 문자열 전처리:
  * 대소문자 정규화(소문자)
  * `|` 또는 `-` 연산자 좌우 공백 정규화
* 허용되는 연산:
  * `SIMPLE`(연산자 없음)
  * `OR`(`a | b`) — **정확히 2개 키워드**
  * `NOT`(`a - b`) — **정확히 2개 키워드**
* 제약/검증:
  * `|`와 `-` 동시 사용 금지 → `BadRequestException`
  * `OR/NOT`에서 한쪽이라도 비어있으면 → `BadRequestException`
  * 키워드는 최대 2개까지 (요구사항 맞춤)

### 4.2 검색 사양(Specification) 규칙

* 위치: `com.trevari.project.search.BookSpecifications`
* 검색 대상 필드: `title`, `subtitle`, `author`, `publisher`
* `contains(kw)`:
  * 공백/빈값이면 **항상 거짓**(disjunction) — SIMPLE=0건, OR/NOT 조합 시 중립화 의도
  * `lower()` + `like "%kw%"` 형태로 매칭
* 전략별 조합:
  * `SIMPLE` → `contains(query)`
  * `OR` → `contains(left) OR contains(right)`
  * `NOT` → `contains(left) AND NOT contains(right)`

### 4.3 예외/에러 정책

* `NotFoundException`: 도서 상세 조회 시 미존재
* `BadRequestException`: 잘못된 쿼리/연산자 조합
* `GlobalExceptionHandler`: 공통 예외 변환(HTTP 상태/메시지 표준화)

## 5) 레포지토리

### BookRepository

* 위치: `com.trevari.project.repository.BookRepository`
* 상속: `JpaRepository<Book, String>`, `JpaSpecificationExecutor<Book>`
* 역할: Book 엔티티 영속성 책임 + Specification 기반 동적 쿼리 처리

## 6) 읽기 모델 / DTO

### SearchDTOs

* 위치: `com.trevari.project.api.dto.SearchDTOs`
* **구성**
  * `Response`: `searchQuery`, `pageInfo`, `books`, `searchMetadata`
  * `PageInfo`: `currentPage`, `pageSize`, `totalPages`, `totalElements`
  * `Book`: 단건 요약(목록용)
  * `Metadata`: `strategy`, `elapsedMs` 등
* 특징: 컨트롤러/서비스에서 곧바로 응답 가능하도록 설계

### BookDetailDTO

* 위치: `com.trevari.project.api.dto.BookDetailDTO`
* 역할: 상세 조회 응답 전용

### SearchKeyword (인기 검색어용)

* 위치: `com.trevari.project.api.dto.SearchKeyword`
* 역할: `(keyword, count)` 쌍으로 TOP10 응답

## 7) 횡단 관심사 (AOP)

### ExecutionTimeAspect

* 위치: `com.trevari.project.aop.ExecutionTimeAspect`, `@MeasureTime`
* 역할: 검색 API 실행 시간을 `elapsedMs`로 계산해 `SearchDTOs.Metadata`에 주입
  (컨트롤러에서 `ResponseEntity<SearchDTOs.Response>`를 반환하는 경우에만 적용)

## 8) 유스케이스 & 흐름

### 8.1 도서 검색 (SIMPLE / OR / NOT)

```
[Client]
   │  GET /api/search/books?q=java|spring&page=1&size=10
   ▼
[BookController]
   ├─ SearchQueryParser.parse(q) → SearchQuery(strategy=OR, left=java, right=spring, query=java|spring)
   ├─ SearchAggregateService.aggregateTop10(parsed.query()) (비동기)
   ├─ SearchService.getSearchDTO(searchQuery, pageable)
   │    ├─ BookSpecifications.forQuery(searchQuery)
   │    ├─ BookRepository.findAll(spec, pageable) → Page<Book>
   │    └─ SearchDTOs.Response 구성(목록/페이지/메타)
   └─ ResponseEntity<SearchDTOs.Response> 반환 (AOP로 elapsedMs 주입)
```

### 8.2 도서 상세 조회

```
[Client] ── GET /api/books/{isbn}
   ▼
[BookController]
   ├─ BookService.getBookDetailDTO(isbn)
   ├─ BookRepository.findById(isbn) or NotFoundException
   └─ BookDetailDTO 반환
```

### 8.3 인기 검색어 TOP10

```
[Client] ── GET /api/analytics/search/top10
   ▼
[BookController]
   ├─ SearchAggregateService.getTop10()
   ├─ Redis ZSET "search:query:popular" 상위 10개 조회
   └─ List<SearchKeyword> 반환
```

## 9) 저장소 & 인프라 모델

* **RDBMS (JPA)**: Book 카탈로그의 단일 진실 소스(SoT)
  * 테이블: `books`
  * 조회는 Specification 기반 동적 조건
* **Redis (ZSET)**: 인기 검색어 집계(쓰기 빠름, 순위 조회 용이)
  * 키: `search:query:popular`
  * 값: member=`query`, score=`count`
  * 읽기는 eventual (집계 비동기)

## 10) 경계(바운디드 컨텍스트)와 의존성

* **Catalog(books)** ↔ **Search**: Search는 Catalog를 조회 대상으로 사용
* **Search** ↔ **Analytics**: Search는 Analytics에 쓰기를 위임(비동기), Analytics는 별도 조회 API 제공
* API 레이어는 응용 서비스(SearchService/BookService, SearchAggregateService)에만 의존

## 11) 확장 포인트

* **검색 필드 확장**: `BookSpecifications.FIELDS`에 컬럼 추가
* **전략 확장**: `SearchStrategy` + `BookSpecifications.forQuery` 조합 추가
* **정렬/스코어링**: 사양 결합 혹은 별도 Query DSL/전용 검색엔진 도입 여지
* **집계 지연/주기 조정**: `@Async` 실행자/큐 정책 변경으로 트래픽 대응
