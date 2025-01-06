# Sosik_Bot
> **Telegram Bot**을 이용한 가상화폐 거래소 이벤트 알림 봇

<br/>

![title](https://github.com/user-attachments/assets/ac9de8d0-6f84-4cc6-bc23-d8ef0824d553)

---

## 📌 개발 배경
가상화폐 거래소에서 진행하는 이벤트를 간편하게 확인하고 싶어 여러 방법을 찾아보았지만, 만족스러운 방법을 찾지 못했습니다. 이에 직접 개발하기로 결심했고, Telegram Bot을 활용한 자동 이벤트 알림 시스템을 구축하게 되었습니다. 이 봇은 24시간 동안 시스템을 가동하여 빠르고 편리하게 이벤트를 확인하고, 사용자가 이벤트에 참여할 수 있도록 돕는 것을 목표로 합니다.

---

## 🎯 개발 목표
- 특정 가상화폐 거래소에서 새로운 이벤트가 등록되면 즉시 알림 제공
- 이벤트 참여 기간이 시작되면 알림 전송
- 이벤트 참여 기간 동안 특정 시간(예: 23시 45분)에 알림 제공
- **AWS 서버**를 활용하여 24시간 안정적으로 시스템 운영

---

## ✅ 현재 구현 기능
### 1. **새로운 Bithumb Airdrop Event 알림 기능**
- **Bithumb 공지사항 페이지**를 크롤링하여 새로운 Airdrop 이벤트 생성 시 알림 전송
- Selenium 라이브러리를 이용한 Bithumb 공지사항 페이지 크롤링
- 크롤링 데이터 중 Airdrop Event에 필요한 데이터만 추출 후 DB 저장
- 스케줄링 작업을 통해 새로운 Airdrop Event 생성 시 알림 기능 구현

<br/>

![new_airdrop_alarm](https://github.com/user-attachments/assets/5c574d0a-2f2d-41cc-9d12-d8592182871f)

### 2. **Bybit LaunchPool Event 알림 기능**
- **Bybit LaunchPool 페이지**를 크롤링하여 새로운 LaunchPool 이벤트 생성 시 알림 전송
- Selenium 라이브러리를 이용한 Bybit 공지사항 페이지 크롤링
- 크롤링 데이터 중 LaunchPool Event에 필요한 데이터만 추출 후 DB 저장
- 스케줄링 작업을 통해 새로운 LaunchPool Event 생성 시 알림 기능 구현

<br/>

![new_launchpool_alarm](https://github.com/user-attachments/assets/132e6350-b911-4b00-be5b-ab8ed58459b7)

### 3. **당일 참여 가능한 Airdrop Event 알림 기능**
- 매일 23시 45분 DB 조회를 통해 당일 참여 가능한 Airdrop Event 알림 기능 구현

<br/>

![airdrop_alarm](https://github.com/user-attachments/assets/49e20e95-f1bf-4e10-a0ed-11a681da80d3)

### 4. **Bithumb Airdrop Event 예측 보상 알림 기능**
- 매일 00시 보상 지급 예정인 Airdrop Event 예측 보상 알림 제공
- Bithumb Open API를 통해 실시간 가상 화폐 가격 조회
- 최근 Airdrop Event 평균 참가자 수를 통한 참가자 수 예측

<br/>

![estimated_airdrop_reward](https://github.com/user-attachments/assets/709bb372-a5f6-483f-a795-1631266f2491)

### 5. **Bithumb Airdrop Event 보상 월말 결산**
- 매월 1일 12시 직전월 Bithumb Airdrop Event 보상 결산 제공
- Airdrop Event 보상 정보를 이미지로 제공

<br/>

![airdrop_reward_report](https://github.com/user-attachments/assets/c9cdd610-e821-43ed-92a8-611dcbc61ec7)
![airdrop_reward_report2](https://github.com/user-attachments/assets/5a672834-9e7c-43b6-848a-adcf07b2c5e8)

---

## ⚙️ 개발 과정
### Test v0.0.1~5
- v0.0.1) Telegram Botfather를 이용한 Telegram Bot 생성
- v0.0.1) Telegram API 라이브러리 통한 Spring Boot 프로젝트와 연결
- v0.0.2) 새로운 Bithumb Airdrop Event 알림 기능 추가
- v0.0.2) AWS 서버에 프로젝트 배포
- v0.0.3) 백그라운드 작업 중 발생한 크롬 프로세스 종료되지 않는 문제 수정
- v0.0.4) 당일 참여 가능한 Airdrop Event 알림 기능 추가 
- v0.0.5) 새로운 Bybit LaunchPool Event 알림 기능 추가

### Release v1.0.0
- Bithumb Airdrop Event 예측 보상 알림 기능 추가
- 새로운 Airdrop Event 알림 기능에 이미지 크롤링 기능 추가
### Release v1.0.1
- 새로운 Airdrop Event 알림 기능에 웹사이트 바로가기 버튼 추가
### Release v1.0.2
- Bithumb Airdrop Event 보상 월말 결산 기능 추가

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
