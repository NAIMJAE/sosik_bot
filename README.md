# Sosik_Bot
> **Telegram Bot**을 이용한 가상화폐 거래소 이벤트 알림 봇

<br/>

![sosikbotmain](https://github.com/user-attachments/assets/837787ac-f237-44fa-9079-170d6b1f09bc)

---

## 📌 개발 배경
가상화폐 거래소에서 진행하는 이벤트를 간편하게 확인하고 싶어 여러 방법을 찾아보았지만, 만족스러운 방법을 찾지 못했습니다.  
이에 직접 개발하기로 결심했고, Telegram Bot을 활용한 자동 이벤트 알림 시스템을 구축하게 되었습니다.  
이 봇은 24시간 동안 시스템을 가동하여 빠르고 편리하게 이벤트를 확인하고, 사용자가 이벤트에 참여할 수 있도록 돕는 것을 목표로 합니다.

---

## 🎯 개발 목표
- 특정 가상화폐 거래소에서 새로운 이벤트가 등록되면 즉시 알림 제공
- 이벤트 참여 기간이 시작되면 알림 전송
- 이벤트 참여 기간 동안 특정 시간(예: 23시 45분)에 알림 제공
- **AWS 서버**를 활용하여 24시간 안정적으로 시스템 운영

---

## ✅ 현재 구현 기능
### 1. **Bithumb Airdrop Event 알림 기능**
- **Bithumb 공지사항 페이지**를 크롤링하여 새로운 Airdrop 이벤트 생성 시 알림 전송
- 이벤트 참여 기간 동안 매일 **23시 45분**에 참여 알림 제공

<br/>

![airdrop_alarm](https://github.com/user-attachments/assets/f99cf774-321f-42ab-b21b-3b54b7888667)
![new_airdrop_alarm](https://github.com/user-attachments/assets/f7983d7a-b546-48b6-b186-98cc51355a0d)

### 2. **Bybit LaunchPool Event 알림 기능**
- **Bybit LaunchPool 페이지**를 크롤링하여 새로운 LaunchPool 이벤트 생성 시 알림 전송

---

## ⚙️ 개발 과정
### Test v0.0.1
- Telegram Botfather를 이용한 Telegram Bot 생성
- Telegram API 라이브러리 통한 Spring Boot 프로젝트와 연결
### Test v0.0.2
- Selenium 라이브러리를 이용한 Bithumb 공지사항 페이지 크롤링 구현
- 크롤링 데이터 중 Airdrop Event 추출 후 DB에 저장
- Scheduled 작업을 통해 새로운 Airdrop Event 생성 시 알림 기능 추가
- AWS 서버에 프로젝트 배포
### Test v0.0.3
- 백그라운드 작업 중 발생한 크롬 프로세스 종료되지 않는 문제 수정
### Test v0.0.4
- 매일 23시 45분 당일 참여 가능한 Airdrop Event 알림 기능 추가 
### Test v0.0.5
- Selenium 라이브러리를 이용한 Bybit 공지사항 페이지 크롤링 구현
- 크롤링 데이터 중 LaunchPool Event 추출 후 DB에 저장
- Scheduled 작업을 통해 새로운 LaunchPool Event가 생성되면 알림 기능 추가

---

## 🚀 추후 개발 목표
- 다른 주요 가상화폐 거래소(예: Binance, Upbit) 이벤트 알림 기능 추가
- 더 세부적인 크롤링과 데이터 분석 기능 도입
- 이벤트 참여와 관련된 유용한 팁 제공 기능 추가

---

## 🌐 기술 스택
- **언어**: Java, Spring Boot
- **라이브러리**: Selenium, Telegram API
- **인프라**: AWS (EC2)
