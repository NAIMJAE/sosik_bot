package com.nemojin.sosikbot.service.crawl;

import com.nemojin.sosikbot.config.WebDriverConfig;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.Notice;
import com.nemojin.sosikbot.service.interfaces.AirdropCrawl;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GopaxCrawling implements AirdropCrawl {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private static final DateTimeFormatter DOT_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.d");
    private final WebDriverConfig webDriver;

    /// [Crawl] Crawling Gopax event List page
    public List<Notice> crawlingEventListPage() throws Exception {
        WebDriver driver = webDriver.getDriver();
        List<Notice> noticeList = new ArrayList<>();

        // Start Crawling
        try {
            driver.get("https://www.gopax.co.kr/notice");

            List<WebElement> noticeItems = driver.findElements(By.cssSelector(".gopax-1iyap1m"));

            // Check the list of notices on the first page
            for (int i = 0; i < noticeItems.size(); i++) {
                // 목록 새로 탐색 (뒤로가면 DOM이 재렌더됨)
                noticeItems = driver.findElements(By.cssSelector(".gopax-1iyap1m"));
                WebElement item = noticeItems.get(i);

                String title = item.findElement(By.cssSelector(".gopax-1jrt941")).getText();
                if (!title.contains("이벤트 안내")) continue;

                String date = item.findElement(By.cssSelector(".gopax-13tlj8d")).getText();

                // 클릭 → 이동 → 현재 URL 수집
                item.click();
                Thread.sleep(2000); // 이동 대기

                String link = driver.getCurrentUrl(); // 실제 라우팅된 URL 추출
                noticeList.add(new Notice(date, link));

                // 뒤로가기 → 목록 복귀
                driver.navigate().back();
                Thread.sleep(2000); // 목록 로딩 대기
            }

        }finally {
            driver.quit();
        }
        return noticeList;
    }

    /// [Crawl] Crawling new Gopax airdrop event detail page
    public List<List<Airdrop>> crawlingAirdropDetail(List<Notice> newList) throws Exception {
        // Create file directory
        String currentDir = Paths.get("").toAbsolutePath().toString();
        File imgDir = new File(currentDir, "img");
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }

        // Create return object
        List<List<Airdrop>> airdropList = new ArrayList<>();

        // Start crawling detail by iterating over each notice
        for (Notice notice : newList) {

            WebDriver driver = webDriver.getDriver();
            List<Airdrop> eventList = new ArrayList<>();

            try {
                driver.get(notice.getLink());

                WebElement detailDiv = driver.findElement(By.cssSelector(".gopax-16axvn4"));
                List<WebElement> divElements = detailDiv.findElements(By.tagName("div"));

                for (WebElement element : divElements) {
                    if (element.getText().equals("<이벤트 일정>")) {
                        String duration = element.findElement(By.xpath("following-sibling::div[1]")).getText();
                        String paymentDate = element.findElement(By.xpath("following-sibling::div[2]")).getText();

                        System.out.println(duration);
                        System.out.println(paymentDate);
                    }
                }


            } finally {
                driver.quit();
            }
            airdropList.add(eventList);
        }
        return airdropList;
    }
}
