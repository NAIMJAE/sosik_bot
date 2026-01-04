package com.nemojin.sosikbot.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

@Configuration
public class WebDriverConfig {
    @Bean
    @Scope("prototype")  // 매번 새로운 WebDriver 인스턴스 생성
    public WebDriver getDriver() {
        // ChromeDriver 경로 설정 (운영체제 선택)
        // System.setProperty("webdriver.chrome.driver", "C:/Users/devimjae/Desktop/chromedriver-win64/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", "/home/ec2-user/sosikbot/chromedriver-linux64/chromedriver");

        // Chrome Option Setting
        ChromeOptions options = new ChromeOptions();

        // Basic Setting
        options.addArguments(
                "--headless",                  // 헤드리스 모드
                "--window-size=1920,1080",       // 창 크기 고정
                "--disable-gpu",                 // GPU 비활성화 (일부 리눅스 환경)
                "--no-sandbox",                  // 샌드박스 비활성화 (권한 문제 해결용)
                "--disable-dev-shm-usage"        // /dev/shm 메모리 부족 방지
        );

        // 브라우저 자동화 감지 회피
        options.addArguments(
                "--disable-blink-features=AutomationControlled",   // 자동화 탐지 비활성화
                "--disable-extensions",                            // 확장 프로그램 비활성화
                "--disable-popup-blocking"                         // 팝업 차단 비활성화
        );
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        // 사용자 에이전트 설정
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/131.0.6778.109 Safari/537.36");

        return new ChromeDriver(options);
    }
}