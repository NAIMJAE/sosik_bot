package com.sosikbot.config;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class WebDriverConfig {
    @Bean
    @Scope("prototype")  // 매번 새로운 WebDriver 인스턴스 생성
    public WebDriver getDriver() {
        // ChromeDriver 경로 설정
        // System.setProperty("webdriver.chrome.driver", "C:/Users/devimjae/Desktop/chromedriver-win64/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", "/home/ec2-user/sosikbot/chromedriver-linux64/chromedriver");
        
        ChromeOptions options = new ChromeOptions();
        // GUI 없는 헤드리스 모드 // 브라우저 창 크기 설정 // GPU 비활성화 (특히 일부 리눅스 환경에서 필요) // 샌드박스 비활성화 (리눅스 권한 문제 해결용) // 메모리 부족 문제 방지
        options.addArguments("--headless", "--window-size=1920,1080", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.109 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled"); // 자동화 감지 비활성화
        options.addArguments("--disable-extensions"); // 확장 프로그램 비활성화
        options.addArguments("--disable-popup-blocking"); // 팝업 차단 비활성화
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        return new ChromeDriver(options);
    }
}
