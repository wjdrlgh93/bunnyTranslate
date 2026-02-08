
<img width="639" height="544" alt="image" src="https://github.com/user-attachments/assets/235b26ad-fb4f-48ed-8da2-2a1b8a963e00" />

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
https://github.com/user-attachments/assets/0a27f59a-36d0-4255-a4a6-48c0858286bc




## 🚀 트러블 슈팅 (Trouble Shooting) & 배운 점

프로젝트를 진행하며 마주친 기술적 문제와 해결 과정입니다.

### 1. Fat Jar 빌드와 Manifest 속성 문제
- **문제:** 로컬에서는 잘 작동하던 봇이 서버 배포 후 `no main manifest attribute` 에러를 발생시키며 실행되지 않음.
- **원인:** 기본 `jar` 빌드 시 의존성 라이브러리(JDA, Gson 등)가 포함되지 않고, Main Class 정보가 Manifest에 명시되지 않음.
- **해결:** Gradle의 **`ShadowJar` 플러그인**을 도입. 모든 의존성을 하나의 Jar 파일로 묶는(Fat Jar) 설정을 추가하고, `manifest { attributes 'Main-Class': '...' }`를 명시하여 해결함.

### 2. OCI 클라우드 방화벽 및 네트워크 설정
- **문제:** 인스턴스 생성 후 SSH 원격 접속 시도 시 `Connection timed out` 발생.
- **원인:** 오라클 클라우드의 기본 VCN Security List(보안 목록)에서 22번 포트에 대한 인바운드 규칙(Ingress Rule)이 부재함.
- **해결:** VCN 설정에서 0.0.0.0/0 (All Traffic)에 대해 TCP 22번 포트를 허용하도록 **Ingress Rule을 수동으로 추가**하여 외부 접속 환경 구축.

### 3. API 보안 및 환경 변수 관리
- **문제:** API 키와 토큰이 코드에 하드코딩되어 있어, 버전 관리 시스템(Git) 업로드 시 보안 위험 발생.
- **해결:** `dotenv-java` 라이브러리를 도입하여 민감한 정보를 `.env` 파일로 분리. `.gitignore` 설정을 통해 깃허브 업로드를 차단하고, 서버 환경에서 별도로 환경 변수를 주입하는 방식으로 **보안 사고를 예방**함.

---

## 💻 설치 및 실행 방법 (How to run)

### 1. 사전 요구 사항
- Java 17 이상
- Discord Bot Token
- DeepL API Key

### 2. 프로젝트 클론
```bash
git clone [https://github.com/wjdrlgh93/bunnyTranslate.git](https://github.com/wjdrlgh93/bunnyTranslate.git)
cd bunnyTranslate


