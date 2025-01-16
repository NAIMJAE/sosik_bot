package com.sosikbot.service.Bybit;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.sosikbot.config.WebDriverConfig;
import com.sosikbot.entity.LaunchPool;
import com.sosikbot.entity.PoolDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class BybitCrawling {

    private final WebDriverConfig webDriver;

    public Map<LaunchPool, List<PoolDetail>> bybitLaunchpoolCrawl(List<LaunchPool> launchPoolLast3) {
        // WebDriver 객체 생성
        WebDriver driver = webDriver.getDriver();

        try {
            driver.get("https://www.bybit.com/en/trade/spot/launchpool");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".NoEndActivites_contentOut__f5swu")));
            List<WebElement> noticeItems = driver.findElements(By.cssSelector(".NoEndActivites_contentOut__f5swu"));

            Map<LaunchPool, List<PoolDetail>> result = new HashMap<>();

            outerLoop:
            for (WebElement item : noticeItems) {

                String title = item.findElement(By.cssSelector(".ActivityCardItem_prizeCoin__E8_0o")).getText();
                WebElement statusElement = item.findElement(By.cssSelector(".ActivityCardItem_timeTop__e4rRr"));
                String date = item.findElement(By.cssSelector(".ActivityCardItem_time__DbRtS")).getText();
                
                // 시작, 끝 날짜 추출
                LocalDateTime[] dateArr = extractDateTimeForBybit(date);

                // 최근의 launchpool과 중복되는지 확인
                for (LaunchPool last3 : launchPoolLast3) {
                    if (last3.getTitle().equals(title) && last3.getStartDate().equals(dateArr[0])) {
                        continue outerLoop;
                    }
                }

                // launchpool의 상태 확인
                String status = statusElement.findElement(By.cssSelector(".ActivityCardItem_countTimeName__62f5S")).getText();
                if (status.contains("Completed")) {
                    continue outerLoop;
                }else {
                    status = "ready";
                }

                // launchpool 객체 생성
                LaunchPool launchpool = new LaunchPool("Bybit", title, status, dateArr[0], dateArr[1]);
                String launchNo = launchpool.getLaunchNo();

                // new launchpool 디테일 정보 추출
                String noticeURL = searchLaunchPoolDatail(title);

                List<PoolDetail> poolDetailList = new ArrayList<>();
                if(noticeURL != null) {
                    poolDetailList = createLaunchPoolDetail(noticeURL, launchNo);
                }else {
                    continue outerLoop;
                }

                result.put(launchpool, poolDetailList);
            }
            return result;
        } catch(Exception e) {
            driver.quit();
            log.error("에러 발생: {}", e.getMessage(), e);

            return null;
        } finally {
            driver.quit();
        }
    }

    // 날짜 추출
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

        // 시작 날짜가 종료 날짜보다 뒤에 있으면 연도를 조정
        if (startDateTime.isAfter(endDateTime)) {
            endDateTime = endDateTime.plusYears(1);
        }

        return new LocalDateTime[]{startDateTime, endDateTime};
    }

    // LaunchPool Datail 탐색
    private String searchLaunchPoolDatail(String title) {
        String noticeURL = null;
        WebDriver driver2 = webDriver.getDriver();

        try {
            outerLoop2:
            for(int i=1; i<15; i++) {
                if (noticeURL != null) {continue;}

                driver2.get("https://announcements.bybit.com/en/?page=" + i);
                WebElement noticeList = driver2.findElement(By.cssSelector(".article-list"));
                List<WebElement> noticeElements = noticeList.findElements(By.tagName("a"));

                for (WebElement notice : noticeElements) {
                    String noticeTitle = notice.getText();

                    if (noticeTitle.contains("Bybit Launchpool") && noticeTitle.contains(title)) {
                        // 주소 추출
                        noticeURL = notice.getAttribute("href");
                        continue outerLoop2;
                    }
                }
            }
            return noticeURL;
        } catch(Exception e) {
            driver2.quit();
            log.error("에러 발생: {}", e.getMessage(), e);

            return null;
        } finally {
            driver2.quit();
        }
    }

    // LaunchPool Detail 생성
    private List<PoolDetail> createLaunchPoolDetail(String noticeURL, String launchNo) {
        List<PoolDetail> poolDetailList = new ArrayList<>();
        WebDriver driver3 = webDriver.getDriver();

        try {
            driver3.get(noticeURL);
            WebElement newTabElement = driver3.findElement(By.cssSelector(".article-detail-content"));

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

            return poolDetailList;

        } catch(Exception e) {
            driver3.quit();
            log.error("에러 발생: {}", e.getMessage(), e);

            return null;
        } finally {
            driver3.quit();
        }
    }
}
