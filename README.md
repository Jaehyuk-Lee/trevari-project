# 도서 검색 API 시스템 (Trevari Project)

> 백엔드 개발자 과제 구현용 백엔드 서비스 (Java Spring Boot 기반)

### 실행 전 준비

- 반드시 `.env` 파일이 필요합니다.
- DB 컨테이너(MySQL)는 `.env`에 정의된 사용자/비밀번호/DB 정보를 기반으로 초기화되며,
  Spring Boot 애플리케이션도 동일한 환경변수를 사용하여 DB에 접속합니다.
- 따라서 `.env` 파일이 없으면 프로젝트를 실행할 수 없습니다.

#### .env.example 파일 사용하기

프로젝트에는 실행 편의를 위해 `.env.example` 파일이 포함되어 있습니다.
이 파일에는 **샘플 값**이 들어 있으며, 그대로 사용하면 로컬 환경에서 바로 실행할 수 있습니다.

1. `.env.example` 파일을 복사하여 `.env`로 이름을 변경합니다.
   ```bash
   cp .env.example .env
    ```

2. 필요하다면 값들을 수정합니다.

   * `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`는 MySQL 컨테이너 초기화 및 Spring Boot DB 접속에 사용됩니다.
   * `MYSQL_ROOT_PASSWORD`는 MySQL 루트 계정 비밀번호입니다.
   * `SPRING_REDIS_HOST`, `SPRING_REDIS_PORT`는 Redis 연결에 사용됩니다.

3. 샘플 값 그대로 두고 실행하면 `docker compose up -d` 후 애플리케이션이 정상적으로 동작합니다.

⚠️ 실제 운영 환경에서는 반드시 `.env`의 값들을 안전한 자격 증명으로 변경해야 합니다.

### EditorConfig

EditorConfig(`.editorconfig`)를 사용해 에디터 설정과 줄바꿈(EOL)을 통일해, 환경별 불필요한 diff 발생을 줄였습니다.

### Docker compose

#### MySQL 설정

서비스 간 의존성을 고려하여 healthcheck 추가 및 depends_on 의존성 설정
