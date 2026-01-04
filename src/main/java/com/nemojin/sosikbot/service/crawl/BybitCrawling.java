package com.nemojin.sosikbot.service.crawl;

import com.nemojin.sosikbot.config.WebDriverConfig;
import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.model.LaunchPool;
import com.nemojin.sosikbot.model.PoolDetail;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class BybitCrawling {
    private final WebDriverConfig webDriver;

    /// [Crawl] Crawling Bybit LaunchPool event page
    public Map<LaunchPool, List<PoolDetail>> crawlingLaunchPoolEventPage(List<LaunchPool> lastEvent) throws Exception {
        WebDriver driver = webDriver.getDriver();

        try {
            driver.get("https://www.bybit.com/en/trade/spot/launchpool");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".EndActivites_activity__ivUjF")));
            List<WebElement> noticeItems = driver.findElements(By.cssSelector(".EndActivites_activity__ivUjF"));

            Map<LaunchPool, List<PoolDetail>> result = new HashMap<>();

            outerLoop:
            for (WebElement item : noticeItems) {

                String title = item.findElement(By.cssSelector(".ActivityCardItem_prizeCoin__E8_0o")).getText();
                WebElement statusElement = item.findElement(By.cssSelector(".ActivityCardItem_contentLeftBottom__JK78g"));
                // WebElement statusElement = item.findElement(By.cssSelector(".ActivityCardItem_timeTop__e4rRr"));
                String date = item.findElement(By.cssSelector(".ActivityCardItem_time__DbRtS")).getText();

                // Extract start date and end date
                LocalDateTime[] dateArr = extractDateTimeForBybit(date);

                // Check duplicates
                for (LaunchPool each : lastEvent) {
                    if (each.getTitle().equals(title) && each.getStartDate().equals(dateArr[0])) {
                        continue outerLoop;
                    }
                }

                // Check launchpool status
                // String status = statusElement.findElement(By.cssSelector(".ActivityCardItem_countTimeName__62f5S")).getText();
                String status = null;
                if (statusElement.getText().contains("Completed")) {
                    continue;
                }else {
                    status = "ready";
                }

                // Create launchpool
                LaunchPool launchpool = new LaunchPool("Bybit", title, status, dateArr[0], dateArr[1]);

                // Search launchpool detail URL
                String noticeURL = searchLaunchPoolDetailURL(title);

                // Create launchpool detail
                List<PoolDetail> poolDetailList = createLaunchPoolDetail(noticeURL, launchpool.getLaunchNo());

                result.put(launchpool, poolDetailList);
            }
            return result;
        } finally {
            driver.quit();
        }
    }

    // [Util] Extract DateTime at Bybit LaunchPool event page
    private LocalDateTime[] extractDateTimeForBybit(String date) {
        String[] dateTimes = date.replace(" UTC", "").split(" ~ ");
        String startDateTimeString = dateTimes[0];
        String endDateTimeString = dateTimes[1];

        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();

        LocalDateTime startDateTime = LocalDateTime.parse(currentYear + "-" + startDateTimeString,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime endDateTime = LocalDateTime.parse(currentYear + "-" + endDateTimeString,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // If the start date is later than the end date, adjust the year
        if (startDateTime.isAfter(endDateTime)) {
            endDateTime = endDateTime.plusYears(1);
        }

        return new LocalDateTime[]{startDateTime, endDateTime};
    }

    // [Util] Search LaunchPool Datail
    private String searchLaunchPoolDetailURL(String title) {
        WebDriver driver = webDriver.getDriver();

        try {
            for(int i=1; i<15; i++) {
                driver.get("https://announcements.bybit.com/en/?page=" + i);

                WebElement noticeList = driver.findElement(By.cssSelector(".article-list"));
                List<WebElement> noticeElements = noticeList.findElements(By.tagName("a"));

                for (WebElement notice : noticeElements) {
                    String noticeTitle = notice.getText();

                    // Extract launchPool event notices Page URL
                    if (noticeTitle.contains("Bybit Launchpool") && noticeTitle.contains(title)) {
                        return notice.getAttribute("href");
                    }
                }
            }
            throw new BusinessException(BotException.NOT_FOUND_LAUNCHPOOL_NOTICE_URL);
        } finally {
            driver.quit();
        }
    }

    // [Util] Create LaunchPool Detail
    private List<PoolDetail> createLaunchPoolDetail(String noticeURL, String launchNo) {
        List<PoolDetail> poolDetailList = new ArrayList<>();
        WebDriver driver = webDriver.getDriver();

        try {
            driver.get(noticeURL);
            WebElement newTabElement = driver.findElement(By.cssSelector(".article-detail-content"));

            List<WebElement> pTag = newTabElement.findElements(By.tagName("p"));
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

            for (WebElement each : pTag) {
                if (each.getText().contains("Total Rewards")) {
                    total.add(each.getText().replaceAll(".*: ", "").trim());
                }
                if (each.getText().contains("Minimum Staking Amount")) {
                    minimum.add(each.getText().replaceAll(".*: ", "").trim());
                }
                if (each.getText().contains("Maximum Staking Amount")) {
                    maximum.add(each.getText().replaceAll(".*: ", "").trim());
                }
            }

            for (int i=0 ; i<name.size() ; i++) {
                PoolDetail poolDetail = new PoolDetail(launchNo, name.get(i), total.get(i), minimum.get(i), maximum.get(i));
                poolDetailList.add(poolDetail);
            }

            return poolDetailList;

        } catch(Exception e) {
            driver.quit();
            throw new BusinessException(BotException.CREATE_LAUNCHPOOL_DETAIL_FAIL);

        } finally {
            driver.quit();
        }
    }
}
