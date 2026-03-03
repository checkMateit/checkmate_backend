# Checkmate Backend

스터디 모집부터 참여, 인증, 기록 관리까지 한 번에 다룰 수 있도록 만든 모바일 스터디 관리 애플리케이션의 MSA 기반 백엔드 서버

<img width="24%"  alt="Image" src="https://github.com/user-attachments/assets/87dbed18-b84c-48e2-bd78-9a79ad73d3cf" />

<img width="24%"   alt="Image" src="https://github.com/user-attachments/assets/7555e246-053c-472b-b5d8-8098da56b775" />

<img width="24%"  alt="Image" src="https://github.com/user-attachments/assets/24a2924f-9668-458f-af04-4d10744f57a4" />

<img width="24%"  alt="Image" src="https://github.com/user-attachments/assets/358ba85b-176f-4704-8e83-627a46f228e5" />


## 🚨 Problem

- 기존 인증 스터디는 카카오톡 오픈채팅, 디스코드, 수기 스프레드시트 등 여러 도구를 함께 써야 해서 운영 흐름이 분산.
- 인증 확인, 미인증 집계, 패널티 반영, 공지 전달 같은 작업이 자동화되지 않아 운영자 부담이 큼.
- GitHub 커밋 인증, 체크리스트 인증, 사진 인증처럼 스터디별 요구사항이 달라도 이를 일관되게 처리할 서버 구조가 필요.

## 🎯 Goal

- 스터디 생성, 참여, 인증, 기록, 포인트, 문의 기능을 MSA 구조로 분리해 안정적으로 운영할 수 있는 백엔드를 구축하는 것이 목표.
- 다양한 인증 방식을 하나의 도메인 모델 안에서 처리할 수 있도록 멀티 인증 스터디 서버를 구현하고자 함.
- 사용자 인증, API 라우팅, 서비스 간 공통 응답 형식까지 통합해 프론트엔드와 운영 기능이 일관되게 동작하는 구조를 지향함.

## 🛠 기능 설명

| 서비스 | 역할 |
| --- | --- |
| `gateway-service` | JWT 인증 필터 적용 및 외부 요청 라우팅 |
| `eureka-service` | 서비스 등록 및 디스커버리 관리 |
| `user-service` | 회원가입, 로그인, OAuth2, JWT, 사용자 정보, 배지 |
| `study-service` | 스터디 생성/참여/검색, 게시판, 인증 기록, GitHub/TODO/사진/위치 인증 |
| `store-service` | 포인트, 상품, 구매, 사용자 아이템, 알림 |
| `community-service` | 공지사항, 문의, 문의 댓글 |
| `common-service` | 공통 DTO, 예외, 공통 엔티티, 보안 유틸 |

## 💻 Tech Stack

- Spring Boot 3
- Spring Cloud Gateway
- Netflix Eureka
- Spring Security
- OAuth2 Client 
- QueryDSL
- PostgreSQL
- Redis
- RabbitMQ


## 🧩 Architecture

- MSA 기반으로 도메인별 서비스를 분리한 구조.
- `gateway-service`가 진입점이 되고, `eureka-service`가 서비스 디스커버리를 담당.
- 각 서비스는 `common-service`를 공유하며 독립적으로 확장 가능하도록 구성.

```text
Client
  |
  v
gateway-service
  |
  +-- user-service
  +-- study-service
  +-- store-service
  +-- community-service
  |
  +-- common-service

eureka-service
  |
  +-- service discovery
```

##  Backend Structure

- `gateway-service`를 기준으로 외부 요청을 받아 인증 필터 적용 후 각 도메인 서비스로 라우팅한다.
- `eureka-service`를 기준으로 각 마이크로서비스의 등록 및 디스커버리를 관리한다.
- 도메인별 기능은 `user-service`, `study-service`, `store-service`, `community-service`로 나누어 관리한다.
- 공통 응답 형식, 예외, 보안 관련 공통 코드는 `common-service`에 모아 재사용한다.

```text
checkmate_backend
├── common-service/           # 공통 DTO, 예외, 공통 엔티티, 보안 유틸
├── community-service/        # 공지사항, 문의, 문의 댓글
├── eureka-service/           
├── gateway-service/          # API 게이트웨이, JWT 인증 필터, 라우팅
├── study-service/            # 스터디 생성/참여/인증/게시판/기록
├── store-service/            # 포인트, 상품, 구매, 알림
├── user-service/             # 회원, OAuth2 로그인, JWT, 배지
├── build.gradle              
├── settings.gradle           # 멀티모듈 구성
├── Dockerfile
├── Jenkinsfile
└── gradlew
```

