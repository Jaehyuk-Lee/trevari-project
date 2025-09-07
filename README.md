# 도서 검색 API 시스템

## 프로젝트 개요

도서 카탈로그를 검색하고, 도서 상세를 조회할 수 있는 REST API 서비스입니다. 인기 검색어 조회 기능도 제공합니다.

## 도메인 모델

도서는 고유 식별자(ISBN)를 가지며 기본 정보와 출판 정보를 포함합니다.

- Book 엔티티 속성 (타입, DB 매핑, 검증)
  - `isbn` (String) — PK, 길이 제한 20, null 불가. 예: "9781617291609"
  - `title` (String) — 길이 제한 255, null 불가.
  - `subtitle` (String) — 길이 제한 255, nullable.
  - `author` (String) — 길이 제한 255, null 불가.
  - `publisher` (String) — 길이 제한 255, nullable.
  - `publishedDate` (LocalDate) — DB 컬럼명 `published_date`, API 응답 필드명: `published` (ISO-8601, yyyy-MM-dd), nullable.
  - `image` (String) — 이미지 URL, 길이 제한 512, nullable.

간단한 API 응답 예시 (Book 객체):
```json
{
  "id": "9781617291609",
  "title": "MongoDB in Action, 2nd Edition",
  "subtitle": "Covers MongoDB version 3.0",
  "author": "Kyle Banker",
  "isbn": "9781617291609",
  "publisher": "Manning",
  "published": "2016-03-01",
  "image": "https://itbook.store/img/books/9781617291609.png"
}
```

검증/비즈니스 노트:
- `isbn`, `title`, `author`는 필수 필드입니다. (엔티티에서 `@NotNull`로 검증)
- `publishedDate`는 ISO-8601 형식(yyyy-MM-dd)으로 직렬화됩니다.
- `isbn`은 외부 키(ISBN 표준) 규격 유효성 검증은 구현되지 않았음(필요시 확장 가능).

### 더 자세한 도메인 설계

자세한 도메인 모델링 문서는 [여기](/docs/domain.md)를 참고하세요.

## API 엔드포인트 예시

- GET /api/books?keyword={keyword}&page={page}&size={size}
- GET /api/books/{id}
- GET /api/search/books?q={query}&page={page}&size={size}
- GET /api/analytics/search/top10

응답 예시(요약):

```json
{
  "searchQuery": "tdd|javascript",
  "pageInfo": { "currentPage": 1, "pageSize": 20, "totalPages": 5, "totalElements": 100 },
  "books": [ /* Book 객체 배열 */ ],
  "searchMetadata": { "executionTime": 23, "strategy": "OR_OPERATION" }
}
```

## 실행 방법

1. `.env` 또는 환경 변수 설정 (`.env.example` 참조)
    - `.env.example` 파일을 그대로 `.env`로 복사해도 작동
2. Docker Compose로 DB 포함 전체 서비스 구동
    - 예: `docker compose up -d`
3. 애플리케이션 접속 및 API 호출 (Swagger UI 또는 Postman 사용)

## API 문서 링크

Docker 컨테이너 실행 이후 접속 가능

- Swagger UI: http://localhost:8080/swagger-ui/index.html

## 초기 데이터

프로젝트는 시드 데이터를 포함합니다. 최소 100건 이상의 샘플 도서가 DB에 적재되도록 초기화 스크립트를 제공합니다.

[data.sql](/src/main/resources/data.sql) 참고

## 기술 스택

- 언어/프레임워크: Java Spring Boot
  - 가장 익숙한 웹 어플리케이션 서버 프레임워크
  - 레이어드 아키텍처를 통한 책임의 분리
  - Spring AOP를 통한 간편한 관심사의 분리
  - 버전: JDK 17 / Spring Boot 3
- 데이터베이스: MySQL
  - 관계형 데이터베이스 중 가장 보편적이고, LIKE 기반 검색이 간단
- 동적 검색 구현: JPA Specification
  - OR / NOT (+AND) 연산자 검색
- 테스트:
  - 단위 및 통합 테스트 :JUnit
- 테스트 환경:
  - RDB 테스트: H2
    - 테스트 환경에서 간단히 데이터베이스를 사용 가능
  - Redis 테스트: Testcontainers (Redis)
    - 개발 환경과 테스트 환경 Redis 분리를 위해 test-containers 도입

## 아키텍처 결정 사항

* **검색 파이프라인**: 쿼리 파싱 → 검색 전략 결정(SIMPLE / OR / NOT) → JPA Specification 조합 → 결과 집계
* **검색 키워드 제한**: 한 번의 요청에서 최대 2개 키워드만 허용
* **동적 검색 구현 방식**: JPQL/QueryDSL 대신 **JPA Specification** 채택
  * 이유: 이번 과제 범위에서는 설정 부담이 적고 빠르게 적용 가능
* **인기 검색어 집계 규칙**: 연산자 검색(`GET /api/search/books`)에서만 집계
* **테스트 환경 분리**
  * RDB: H2 (MySQL 호환 모드)
  * Redis: Testcontainers (개발 환경과 격리)

## 테스트

테스트은 단위 테스트와 일부 통합 테스트를 포함합니다.

### 테스트 실행 방법

Docker가 실행되고 있어야 합니다.

`./gradlew test`로 실행 가능합니다.

### 단위 테스트 대상

- Controller
  - BookController
- Service
  - BookService
  - SearchService
- SearchQueryParser
- Aspect
  - ExecutionTimeAspect

### 슬라이스 테스트 대상

- Repository
  - BookSpecification + BookRepository + H2
- SearchAggregateService (인기 검색어 Redis 집계 및 조회 서비스)
  - SearchAggregateService + Redis

### 통합 테스트 대상

- BookE2ETest
  - 단건 조회 API E2E
  - 단순 검색 응답 확인
  - 연산자 검색 -> Redis 집계 반영 및 인기검색어 확인

## 문제 해결 및 고민한 점

### JPA Specification

- 현재: 정적 쿼리와 JPQL은 이미 알고 있는 상황
- 도전: JPA Specification이라는 새로운 기술을 도입해보는 것이 효과적일까?

기술 난이도 낮음 ← **정적 쿼리** - **JPQL** - JPA Specification - QueryDSL → 복잡도가 높은 프로젝트에서 유지보수성 향상

#### 기술 탐색

- JPA Specification 장단점 확인
- 사용 예시를 ChatGPT에게 요청

**장점**: 조건 조립 유연성

#### 검색 조건

- 5가지 검색 조건("isbn", "title", "subtitle", "author", "publisher") 사용
- OR 연산과 NOT (+AND) 연산 조립 필요

**결정**: Specification을 활용해 필요에 따라 조건을 조립

### 검색 엔드포인트 분리

과제 예시에 다음과 같이 검색 엔드포인트가 두 가지로 분리되어 제시되었습니다.

```
GET /api/books?keyword={keyword}
GET /api/search/books?q={query}
```

따라서, 검색 기능을 두 가지로 분리해보았습니다.

#### 연산자 검색

검색 연산자를 허용하는 검색 기능

`GET /api/search/books?q={query}`에 요청하면, 연산자를 판독해 `SIMPLE`/`OR`/`NOT` 검색 전략 중에서 선택합니다.

#### 단순 검색

키워드만 입력받는 단순 검색 기능

`GET /api/books?keyword={keyword}`에 요청하면, 연산자가 포함되어도 항상 `SIMPLE` 전략으로 검색을 진행합니다.

### 인덱스

#### 문제

LIKE '%kw%' 형태의 검색은 인덱스를 활용하기 어렵고, 데이터가 커질수록 성능 문제가 발생합니다.

#### 대안

- **FULLTEXT 인덱스**: 단어 단위로 인덱싱 → 완벽한 부분 검색 기능에 적절치 않음
- **N-gram 인덱스**: N글자 단위로 잘라 인덱싱 → 부분 문자열 검색은 가능하나 인덱스 크기와 성능 비용이 큼
- **ElasticSearch**: 대용량 데이터를 사용해서 인덱싱 기능이 반드시 필요하다면 활용 고려 가능
  - 프로젝트 규모에 비해 기술 도입 비용이 과다하다고 판단

#### 결정

이번 과제에서는 소규모 데이터를 다루므로 인덱스를 적용하지 않았습니다.

### 인기검색어 집계

- 상황: 같은 문자열로 검색해도 '단순 검색' 혹은 '연산자 검색'에 따라 검색 결과가 달라짐
- 문제:
  - 단순 검색 `mongo-test`와 연산자 검색 `mongo-test`는 서로 다른 검색. 하지만 검색어는 동일.
  - 따라서, 사용자 입장에서 인기검색어가 정확히 어떤 검색인지 알기 어려움.
- 해결: 연산자 검색을 사용하는 경우에만 인기 검색어를 집계하도록 설계

이에 관한 이슈: [#29](https://github.com/Jaehyuk-Lee/trevari-project/issues/29)

### NOT (+AND) 검색 관련

- 발견: Repository 슬라이스 테스트 도중 NOT 연산과 관련된 테스트가 실패하는 것을 확인
- 문제: NULL 값이 포함된 필드의 NOT 검색 결과가 UNKNOWN 처리되어, 필드간 후속 OR 연산에서 전체 쿼리 결과를 오염
- 해결: NOT 조건 검색 결과에 COALESCE를 통해 NULL 대신 "" 빈 문자열을 사용하도록 변경

### Testcontainers

- 문제: 테스트를 진행하면 개발 환경의 Redis에 부작용을 남김
- 해결: 격리된 테스트 환경 필요성 확인. Testcontainers로 해결이 가능하여 빠르게 도입

### 검색 쿼리의 모호성 처리

일반적인 검색:
- 키워드 양옆의 공백은 무시
- 대소문자 구분하지 않음

### 기타

- MacOS 환경에서는 .env 인라인 주석 허용하지 않음. 행주석 사용. - [#23](https://github.com/Jaehyuk-Lee/trevari-project/pull/23)
