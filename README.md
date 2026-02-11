
<img width="639" height="544" alt="image" src="https://github.com/user-attachments/assets/235b26ad-fb4f-48ed-8da2-2a1b8a963e00" />

# 업데이트 일람 

2/10 버그픽스 <br/>
2/11 일->영 번역 기능 추가 <br/>
------
# 🐰 BunnyTranslate (Discord Global Bridge)

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/JDA-5.0.0-7289DA?style=for-the-badge&logo=discord&logoColor=white)
![Oracle Cloud](https://img.shields.io/badge/Oracle_Cloud-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

> **Oracle Cloud Infrastructure(OCI) 기반의 고가용성 디스코드 실시간 한-일 번역 서비스**

## 📖 프로젝트 소개
**BunnyTranslate**는 언어 장벽 없이 소통할 수 있도록 돕는 디스코드 봇 서비스입니다.  
단순한 API 호출을 넘어, **실제 운영 가능한 프로덕션 환경**을 구축하는 것을 목표로 개발되었습니다.  
Java 기반의 비동기 처리와 Oracle Cloud를 활용한 클라우드 배포를 통해 24시간 중단 없는 서비스를 제공합니다.

### 🎯 개발 목표
- **안정적인 서비스 운영:** 로컬 환경을 넘어 클라우드(Linux/Ubuntu) 환경에서의 배포 및 프로세스 관리
- **외부 API 최적화:** DeepL API v2 연동 및 HTTP Header 인증 방식을 통한 보안 강화
- **빌드 자동화:** Gradle 및 ShadowJar를 활용한 Fat Jar 빌드 파이프라인 구축

---

## 💡 프로젝트 핵심 기능 (Key Features)

### 1. Context Menu (User Apps) 기반의 UX 개선
- **기존 방식의 한계:** 사용자가 번역하고 싶은 문장을 복사해서 `!번역 [문장]` 형태로 명령어를 다시 입력해야 하는 불편함 존재.
- **개선:** Discord의 **Interaction API**를 활용하여, 메시지 **우클릭(Right-Click) → 앱(Apps)** 메뉴에서 즉시 번역할 수 있는 기능을 구현.
- **기술적 구현:** `MessageContextInteractionEvent`를 핸들링하여 비동기(Async)로 DeepL API를 호출하고, `deferReply()`를 통해 API 응답 지연 시간 동안의 타임아웃을 방지함.

### 2. 프로덕션 레벨의 배포 및 보안 구축
- **클라우드 인프라:** Oracle Cloud (Ubuntu 24.04 LTS) 인스턴스에 배포하여 24시간 중단 없는 서비스 제공.
- **백그라운드 프로세스:** `GNU Screen`을 활용하여 세션이 끊겨도 서비스가 유지되도록 데몬(Daemon)화 처리.
- **보안(Security):** 민감한 API Key와 Token은 `.env` 파일로 분리하여 관리. `.gitignore`를 통해 저장소 유출을 방지하고, 서버 배포 시에는 SCP를 통해 환경 변수를 별도 주입하는 보안 파이프라인 구축.

---

## 🛠 기술 스택 (Tech Stack)

### Backend
- **Language:** Java 17 (LTS)
- **Framework:** JDA (Java Discord API)
- **Build Tool:** Gradle (Groovy)
- **External API:** DeepL API v2 (Translation)

### Infrastructure & DevOps
- **Cloud Provider:** Oracle Cloud Infrastructure (OCI) Always Free
- **OS:** Ubuntu 22.04 LTS (ARM/AMD Architecture)
- **Network:** VCN (Virtual Cloud Network), Security List Configuration
- **Deployment:** SSH, SCP, GNU Screen (Background Process Management)
- **Security:** Dotenv (.env) for Environment Variable Management

---

## 🏗 아키텍처 및 워크플로우

1. **사용자 요청:** 디스코드 채널에서 명령어(`!81`, `!82`) 입력
2. **이벤트 감지:** JDA Listener가 메시지 이벤트를 비동기로 수신
3. **번역 처리:** `HttpClient`를 통해 DeepL API 서버로 요청 (Header Auth 방식)
4. **응답 및 전송:** 번역된 텍스트를 파싱(Gson)하여 디스코드 채널로 실시간 전송
5. **인프라:** OCI VM 인스턴스에서 `Screen`을 통해 데몬 형태로 상주하며 실행

---
<br><br>
![녹음 2026-02-08 120310](https://github.com/user-attachments/assets/0606f6d9-86d4-48c7-bee6-844fc52193f8)<br>
<img width="1541" height="721" alt="546599989-7eac8fcb-24f1-4c80-b28d-0b3e06d37c64" src="https://github.com/user-attachments/assets/f3836a4e-ce11-49f7-89c6-eb0759783c9c" />
<br>



## 🚀 트러블 슈팅 (Trouble Shooting) & 배운 점

프로젝트를 진행하며 마주친 기술적 문제와 해결 과정입니다.

### 📌 1. Fat Jar 빌드와 Manifest 속성 문제
- **문제:** 로컬에서는 잘 작동하던 봇이 서버 배포 후 `no main manifest attribute` 에러를 발생시키며 실행되지 않음.
- **원인:** 기본 `jar` 빌드 시 의존성 라이브러리(JDA, Gson 등)가 포함되지 않고, Main Class 정보가 Manifest에 명시되지 않음.
- **해결:** Gradle의 **`ShadowJar` 플러그인**을 도입. 모든 의존성을 하나의 Jar 파일로 묶는(Fat Jar) 설정을 추가하고, `manifest { attributes 'Main-Class': '...' }`를 명시하여 해결함.

### 📌 2. OCI 클라우드 방화벽 및 네트워크 설정
- **문제:** 인스턴스 생성 후 SSH 원격 접속 시도 시 `Connection timed out` 발생.
- **원인:** 오라클 클라우드의 기본 VCN Security List(보안 목록)에서 22번 포트에 대한 인바운드 규칙(Ingress Rule)이 부재함.
- **해결:** VCN 설정에서 0.0.0.0/0 (All Traffic)에 대해 TCP 22번 포트를 허용하도록 **Ingress Rule을 수동으로 추가**하여 외부 접속 환경 구축.


### 📌 3. API 보안 및 환경 변수 관리
- **문제:** API 키와 토큰이 코드에 하드코딩되어 있어, 버전 관리 시스템(Git) 업로드 시 보안 위험 발생.
- **해결:** `dotenv-java` 라이브러리를 도입하여 민감한 정보를 `.env` 파일로 분리. `.gitignore` 설정을 통해 깃허브 업로드를 차단하고, 서버 환경에서 별도로 환경 변수를 주입하는 방식으로 **보안 사고를 예방**함.

### 📌 4. JDA 4 → 5 마이그레이션과 심볼 참조 오류
- **문제 상황:** 우클릭 메뉴(`Context Menu`) 구현을 위해 코드를 작성했으나, `CommandType` 심볼을 찾지 못하는 컴파일 에러 발생.
- **원인 분석:** 기존에 사용하던 JDA 4.x 버전은 Interaction API의 최신 기능을 지원하지 않음. 또한 JDA 5.x로 오면서 `CommandType`이 `Command.Type` 내부 Enum으로 구조가 변경됨.
- **해결:**
    1. `build.gradle` 의존성을 `5.0.0-beta.24`로 업그레이드.
    2. Deprecated된 메서드를 제거하고, `Command.Type.MESSAGE` 등 변경된 API 명세에 맞춰 리팩토링 진행.
    3. Gradle 캐시 초기화(`Invalidate Caches`)를 통해 IDE의 인덱싱 동기화 문제 해결.

### 📌 5.  Discord OAuth2 Scope 권한 문제
- **문제 상황:** 코드는 정상적으로 배포되었고 로그상으로도 등록 성공(`Success`)이 떴으나, 실제 디스코드 클라이언트에서 메뉴가 노출되지 않음.
- **원인 분석:** 봇을 초대할 때 기본 권한(`bot`)만 부여하고, Slash Command 및 Context Menu 사용을 위한 **`applications.commands` 스코프(Scope)**가 누락됨을 확인. Discord API 정책상 코드로 기능을 구현해도 초기 초대 시 해당 권한이 없으면 UI에 렌더링되지 않음.
- **해결:**
    1. Discord Developer Portal에서 `applications.commands` 권한이 포함된 초대 링크를 재생성.
    2. 기존 봇을 서버에서 추방하지 않고 재승인(Re-authorize)하여 권한을 덮어씌움으로써 서비스 중단 없이 해결.

### 📌 6. 프로덕션 환경에서의 환경변수 누락 (`.env`)
- **문제 상황:** 로컬 빌드 후 서버에서 `java -jar` 실행 시 `InvalidTokenException` 발생하며 즉시 종료.
- **원인 분석:** 보안을 위해 `.env` 파일을 Git에 포함하지 않았는데, 배포 과정에서 Jar 파일만 전송하고 `.env` 파일을 서버에 생성하지 않아 발생한 런타임 에러.
- **해결:** 리눅스 서버 내에서 `nano` 에디터를 통해 로컬과 동일한 환경 변수 파일을 수동 생성하고, 파일 권한을 제한하여 보안성을 확보함.

---





