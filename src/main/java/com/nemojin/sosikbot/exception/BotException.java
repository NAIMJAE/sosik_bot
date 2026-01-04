package com.nemojin.sosikbot.exception;

import lombok.Getter;

@Getter
public enum BotException {
    // MESSAGE
    SEND_MESSAGE_FAIL("MESSAGE ERROR", "SEND MESSAGE FAIL", "메세지 전송 실패"),
    NOT_FOUND_EXCHANGE("MESSAGE ERROR", "NOT FOUND EXCHANGE", "거래소 매칭 실패"),
    CREATE_REPORT_IMAGE_FAIL("MESSAGE ERROR", "CREATE_REPORT_IMAGE_FAIL", "월간 레포트 이미지 생성 실패"),
    
    // AIRDROP CRAWLING
    NOT_FOUND_DATE("CRAWLING ERROR", "NOT FOUND DATE", "공지사항에서 날짜 추출 실패"),
    NOT_FOUND_TITLE("CRAWLING ERROR", "NOT FOUND TITLE", "공지사항에서 제목 추출 실패"),
    NOT_FOUND_REWARD_INFO("CRAWLING ERROR", "NOT FOUND REWARD INFO", "공지사항에서 리워드 단위와 액수 추출 실패"),
    NOT_FOUND_IMAGE("CRAWLING ERROR", "NOT FOUND IMAGE", "공지사항에서 이미지 추출 실패"),

    // LAUNCHPOOL CRAWLING
    NOT_FOUND_LAUNCHPOOL_NOTICE_URL("CRAWLING ERROR", "NOT FOUND LAUNCHPOOL NOTICE URL", "LAUNCHPOOL 공지사항 주소 추출 실패"),
    CREATE_LAUNCHPOOL_DETAIL_FAIL("CRAWLING ERROR", "CREATE LAUNCHPOOL DETAIL FAIL", "LAUNCHPOOL 세부 정보 생성 실패"),

    // IMAGE DOWNLOAD
    IMAGE_DOWNLOAD_FAIL("IMAGE DOWNLOAD ERROR", "IMAGE DOWNLOAD FAIL", "이미지 다운로드 실패"),
    IMAGE_CONNECTION_FAIL("IMAGE DOWNLOAD ERROR", "IMAGE CONNECTION FAIL", "이미지 다운로드 중 HTTP 연결 실패"),
    IMAGE_FORMAT_NOT_SUPPORTED("IMAGE DOWNLOAD ERROR", "IMAGE FORMAT NOT SUPPORTED", "이미지 다운로드 중 지원하지 않는 이미지 형식"),
    IMAGE_ALREADY_EXISTS("IMAGE DOWNLOAD ERROR", "IMAGE ALREADY EXISTS", "이미지 다운로드 중 이미 존재하는 이미지"),

    // API
    NOT_FOUND_EXCHANGE_TICKER("API ERROR", "NOT FOUND EXCHANGE TICKER", "Ticker 정보 획득을 위한 API 연결 시도 중 거래소 매칭 실패"),
    BITHUMB_TICKER_CONNECTION_FAIL("API ERROR", "BITHUMB TICKER CONNECTION FAIL", "Bithumb Ticker API 연결 실패"),
    BITHUMB_TICKER_PARSE_FAIL("API ERROR", "BITHUMB TICKER PARSE FAIL", "Bithumb Ticker 정보 추출 실패"),
    BITHUMB_API_REQUEST_FAIL("API ERROR", "BITHUMB API REQUEST FAIL", "Bithumb API 요청 실패"),
    BITHUMB_API_CREATE_FAIL("API ERROR", "BITHUMB API CREATE FAIL", "Bithumb API 요청 객체 생성 실패"),
    JSON_PARSING_FAIL("API ERROR", "JSON PARSING FAIL", "Bithumb 입금 정보 JSON 파싱 실패"),
    AUTO_TRADING_ERROR("API ERROR", "AUTO TRADING ERROR", "Bithumb 자동 매매 실패"),
    TRADE_SUCCESS_FAIL("API ERROR", "TRADE SUCCESS FAIL", "Bithumb 자동 매매 주문 성사 실패"),

    //
    NOT_FOUND_PAGE("CRAWL ERROR", "NOT FOUND PAGE", "크롤링 페이지 접근 실패"),
    
    ;

    private final String type;
    private final String content;
    private final String opinion;

    BotException(String type, String content, String opinion) {
        this.type = type;
        this.content = content;
        this.opinion = opinion;
    }
}
