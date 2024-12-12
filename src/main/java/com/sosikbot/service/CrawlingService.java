package com.sosikbot.service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.sosikbot.entity.Airdrop;
import com.sosikbot.entity.LaunchPool;
import com.sosikbot.entity.PoolDetail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CrawlingService {
    
    private static final Logger logger = LoggerFactory.getLogger(CrawlingService.class);

    public List<Airdrop> bithumbAirdropCrawl() {
        // ChromeDriver 경로 설정
        //System.setProperty("webdriver.chrome.driver", "C:/Users/devimjae/Desktop/chromedriver-win64/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", "/home/ec2-user/sosikbot/chromedriver-linux64/chromedriver");

        // ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.109 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled"); // 자동화 감지 비활성화
        options.addArguments("--start-maximized"); // 브라우저 최대화
        options.addArguments("--disable-extensions"); // 확장 프로그램 비활성화
        options.addArguments("--disable-popup-blocking"); // 팝업 차단 비활성화
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        // WebDriver 객체 생성
        WebDriver driver = new ChromeDriver(options);

        try {
            // 크롤링할 URL로 이동
            driver.get("https://feed.bithumb.com/notice");

            // 공지사항 항목 읽기
            List<WebElement> noticeItems = driver.findElements(By.cssSelector(".NoticeContentList_notice-list__i337r li"));
            
            // 결과를 저장할 리스트
            List<Airdrop> airdropList = new ArrayList<>();

            // 날짜 변환을 위한 포맷터
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

            // 공지사항 제목과 링크 필터링
            for (WebElement item : noticeItems) {
                String title = item.findElement(By.cssSelector(".NoticeContentList_notice-list__link-title__nlmSC")).getText();
                String link = item.findElement(By.tagName("a")).getAttribute("href");

                // "에어드랍"이 포함된 제목 필터링
                if (title.contains("에어드랍 이벤트")) {
                    // 상세 페이지로 이동
                    WebDriver driver2 = new ChromeDriver(options);
                    try {
                        driver2.get(link);

                    WebElement noticeDetailDiv = driver2.findElement(By.cssSelector(".NoticeDetailContent_detail-content__notice__X5VPQ"));
                    List<WebElement> ulElements = noticeDetailDiv.findElements(By.tagName("ul"));

                    LocalDate startDate = null;
                    LocalDate endDate = null;
                    String content = "";

                    for (int i=1; i<ulElements.size()-1; i++) {

                        List<WebElement> ilElements = ulElements.get(i).findElements(By.tagName("li"));

                        for (WebElement li : ilElements) {

                            String text = li.getText();

                            if (text.contains("기간 : ")) {

                                String[] parts = text.split("\\s+");

                                for (String part : parts) {

                                    if (part.matches("\\d{4}\\.\\d{2}\\.\\d{2}\\(.*?\\)")) {
                                        
                                        // 괄호와 요일 제거
                                        String dateOnly = part.replaceAll("\\(.*?\\)", "");
                                        
                                        if (startDate == null) {
                                            startDate = LocalDate.parse(dateOnly, formatter);
                                        } else {
                                            endDate = LocalDate.parse(dateOnly, formatter);
                                        }
                                    }
                                }
                            }else if (li.getText().contains("대상 : ")) {
                                content = text;
                            }
                        }
                    }
                    if (startDate != null && endDate != null) {
                        Airdrop airdrop = new Airdrop("Bithumb", title, content, startDate, endDate);
                        airdropList.add(airdrop);
                    }else {
                        Airdrop airdrop = new Airdrop("Bithumb", title, content, null, null);
                        airdropList.add(airdrop);
                    }
                    } catch(Exception e) {
                        driver2.quit();
                        logger.error("에러 발생 : ", driver2);
                        logger.error(e.getMessage(), e);
                    } finally {
                        driver2.quit();
                    }
                }
            }
            return airdropList;
        } catch(Exception e) {
            driver.quit();
            logger.error("에러 발생 : ", driver);
            logger.error(e.getMessage(), e);

            return null;
        } finally {
            driver.quit();
        }
    }

    public Map<LaunchPool, List<PoolDetail>> bybitLaunchpoolCrawl(List<LaunchPool> launchPoolLast3) {
        //System.setProperty("webdriver.chrome.driver", "C:/Users/devimjae/Desktop/chromedriver-win64/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", "/home/ec2-user/sosikbot/chromedriver-linux64/chromedriver");

        // ChromeOptions 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // GUI 없는 헤드리스 모드
        options.addArguments("--window-size=1920,1080"); // 브라우저 창 크기 설정
        options.addArguments("--disable-gpu"); // GPU 비활성화 (특히 일부 리눅스 환경에서 필요)
        options.addArguments("--no-sandbox"); // 샌드박스 비활성화 (리눅스 권한 문제 해결용)
        options.addArguments("--disable-dev-shm-usage"); // 메모리 부족 문제 방지

        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.109 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.bybit.com/en/trade/spot/launchpool");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".EndActivites_activity__ivUjF")));

            List<WebElement> noticeItems = driver.findElements(By.cssSelector(".EndActivites_activity__ivUjF"));

            Map<LaunchPool, List<PoolDetail>> result = new HashMap<>();

            outerLoop:
            for (WebElement item : noticeItems) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", item);
                Thread.sleep(500);

                String title = item.findElement(By.cssSelector(".ActivityCardItem_prizeCoin__E8_0o")).getText();
                String status = item.findElement(By.cssSelector(".ActivityCardItem_completed__RJoI5")).getText();
                String date = item.findElement(By.cssSelector(".ActivityCardItem_time__DbRtS")).getText();
                List<WebElement> btnList = item.findElements(By.cssSelector(".ActivityCardItem_tagText__JfIrg"));
                
                LocalDateTime[] dateArr = extractDateTimeForBybit(date);

                for (LaunchPool last3 : launchPoolLast3) {
                    if (last3.getTitle().equals(title) && last3.getStartDate().equals(dateArr[0])) {
                        continue outerLoop;
                    }
                }

                LaunchPool launchpool = new LaunchPool("Bybit", title, status, dateArr[0], dateArr[1]);
                String launchNo = launchpool.getLaunchNo();

                List<PoolDetail> poolDetailList = new ArrayList<>();

                for (WebElement btn : btnList) {
                    if (btn.getText().equals("Rules")) {
                        // 현재 창의 핸들 저장
                        String originalWindow = driver.getWindowHandle();
                        // Rules 버튼 클릭
                        btn.click();
                        // 새 창이 열릴 때까지 대기
                        WebDriverWait newTabWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                        newTabWait.until(ExpectedConditions.numberOfWindowsToBe(2));

                        // 모든 창의 핸들 가져오기
                        for (String windowHandle : driver.getWindowHandles()) {
                            if (!windowHandle.equals(originalWindow)) {
                                driver.switchTo().window(windowHandle);
                                break;
                            }
                        }

                        WebElement newTabElement = driver.findElement(By.cssSelector(".article-detail-content"));
                        List<WebElement> ptag = newTabElement.findElements(By.tagName("p"));
                        List<WebElement> strong = newTabElement.findElements(By.tagName("strong"));

                        List<String> name = new ArrayList<>();
                        List<String> total = new ArrayList<>();
                        List<String> minimum = new ArrayList<>();
                        List<String> maximum = new ArrayList<>();

                        for (WebElement eachSt : strong) {
                            if (eachSt.getText().contains("Pool")) {
                                name.add(eachSt.getText().replaceAll("^\\d+\\.\\s*", ""));
                            }
                        }

                        for (WebElement eachPtag : ptag) {
                            if (eachPtag.getText().contains("Total Rewards")) {
                                total.add(eachPtag.getText().replaceAll(".*: ", "").trim());
                            }
                            if (eachPtag.getText().contains("Minimum Staking Amount")) {
                                minimum.add(eachPtag.getText().replaceAll(".*: ", "").trim());
                            }
                            if (eachPtag.getText().contains("Maximum Staking Amount")) {
                                maximum.add(eachPtag.getText().replaceAll(".*: ", "").trim());
                            }
                        }

                        for (int i=0 ; i<name.size() ; i++) {
                            PoolDetail poolDetail = new PoolDetail(launchNo, name.get(i), total.get(i), minimum.get(i), maximum.get(i));
                            poolDetailList.add(poolDetail);
                        }

                        // 새로운 탭 닫기
                        driver.close();
                        // 원래 창으로 돌아가기
                        driver.switchTo().window(originalWindow);
                    }
                }
                result.put(launchpool, poolDetailList);
            }
            return result;
        } catch(Exception e) {
            driver.quit();
            logger.error("에러 발생: {}", e.getMessage(), e);

            return null;
        } finally {
            driver.quit();
        }
    }

    public LocalDateTime[] extractDateTimeForBybit(String date) {
        String[] dateTimes = date.replace(" UTC", "").split(" ~ ");
        String startDateTimeString = dateTimes[0];
        String endDateTimeString = dateTimes[1]; 

        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();

        LocalDateTime startDateTime = LocalDateTime.parse(currentYear + "-" + startDateTimeString, 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime endDateTime = LocalDateTime.parse(currentYear + "-" + endDateTimeString, 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return new LocalDateTime[]{startDateTime, endDateTime};
    }
}
