package com.nemojin.sosikbot.service.crawl;

import com.nemojin.sosikbot.config.WebDriverConfig;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.Notice;
import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.service.interfaces.AirdropCrawl;
import com.nemojin.sosikbot.util.DateParser;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.nemojin.sosikbot.util.ImageHelper;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@RequiredArgsConstructor
@Service
public class BithumbCrawling implements AirdropCrawl {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private static final DateTimeFormatter DOT_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.d");
    private static final Pattern CONSECUTIVE_DAY_PATTERN = Pattern.compile("(\\d+)일");
    private final WebDriverConfig webDriver;

    /// [Crawl] Crawling Bithumb event List page
    public List<Notice> crawlingEventListPage() throws Exception {
        WebDriver driver = webDriver.getDriver();
        List<Notice> noticeList = new ArrayList<>();

        // Start Crawling
        try {
            driver.get("https://feed.bithumb.com/notice?category=8&page=1");

            List<WebElement> noticeItems = driver.findElements(By.cssSelector(".NoticeContentList_notice-list__i337r li"));
            if (noticeItems.isEmpty()) {
                throw new BusinessException(BotException.NOT_FOUND_PAGE);
            }

            // Check the list of notices on the first page
            for (WebElement item : noticeItems) {
                String title = item.findElement(By.cssSelector(".NoticeContentList_notice-list__link-title__nlmSC")).getText();
                if (!title.contains("에어드랍 이벤트")) {continue;}

                String noticeDate = item.findElement(By.cssSelector(".NoticeContentList_notice-list__link-date__gDc6U")).getText();
                LocalDate date = DateParser.stringToLocalDate(noticeDate);
                if (!date.isEqual(LocalDate.now())) {continue;}

                String link = item.findElement(By.tagName("a")).getAttribute("href");

                noticeList.add(new Notice(noticeDate, link));
            }

            return noticeList;
        } finally {
            driver.quit();
        }
    }

    /// [Crawl] Crawling new Bithumb airdrop event detail page
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

                WebElement detailDiv = driver.findElement(By.cssSelector(".NoticeDetailContent_detail-content__notice__X5VPQ"));
                List<WebElement> pElements = detailDiv.findElements(By.tagName("p"));
                List<WebElement> ulElements = detailDiv.findElements(By.cssSelector("ul[style='list-style-type: square;']"));

                Airdrop airdrop = new Airdrop();

                // Iterate over each <ul> element
                outerLoop:
                for (WebElement element : ulElements) {
                    if (element.getText().contains("입금 & 매도")) {continue;}

                    List<WebElement> liElements = element.findElements(By.tagName("li"));

                    if (liElements.isEmpty()) {continue;}

                    if (airdrop.hasTitle()) {
                        String title = extractTitleFromNotice(element);
                        //String coin = title.replaceAll(".*\\(([^)]*)\\).*", "$1");
                        String coin = title.replaceAll(".*\\(([^)]+)\\).*", "$1");
                        airdrop.updateTitleAndCoin(title, coin);
                    }

                    if (airdrop.getTitle().contains("순입금")) {continue;}

                    for (WebElement li : liElements) {
                        String text = li.getText();

                        if (text.contains("기간 : ")) {
                            LocalDate[] dates = extractDateRangeFromString(text);
                            airdrop.updateDateRange(notice.getDate(), dates[0], dates[1]);

                        } else if (text.contains("대상 : ")) {
                            String type = "Market";
                            int day = 1;

                            if (text.contains("순입금")) {
                                airdrop.deleteTitle();
                                continue outerLoop;
                            }

                            if (text.contains("메이커") || text.contains("Maker")) {
                                type = "Limit";
                            } else if (text.contains("쿠폰코드")) {
                                type = "Coupon";
                            }

                            Matcher matcher = CONSECUTIVE_DAY_PATTERN.matcher(text);
                            if (matcher.find()) {day = Integer.parseInt(matcher.group(1));}

                            airdrop.updateContentTypeAndConsecutive(text, type, day);

                        } else if (text.contains("보상 : ")) {
                            String[] rewardInfo = extractRewardFromString(text);
                            airdrop.updateRewardInfo(rewardInfo[0], Integer.parseInt(rewardInfo[1]));

                        } else if (text.contains("이벤트 지급일 : ")) {
                            LocalDate date = extractPaymentDateFromString(text);
                            airdrop.updatePaymentDate(date);
                        }
                    }

                    // Extract event image
                    if (airdrop.isComplete()) {
                        String imageUrl = extractImageFromNotice(pElements);
                        boolean result = ImageHelper.downloadImageFromUrl(imgDir, imageUrl, airdrop.getTitle(), airdrop.getDate());

                        if (result) {
                            airdrop.updateExchangeAndUrl("Bithumb", notice.getLink());
                            eventList.add(airdrop);

                            logger.info("SUCCESS CRAWLING :: " + airdrop.getTitle());
                            airdrop = new Airdrop();
                        }
                    }
                }
            } finally {
                driver.quit();
            }
            if (!eventList.isEmpty()) {
                airdropList.add(eventList);
            }
        }
        return airdropList;
    }

    /// [Util] Extract start Date and End Date by Airdrop Notice
    private LocalDate[] extractDateRangeFromString(String text) {
        String[] parts = text.split("\\s+");
        LocalDate startDate = null;
        LocalDate endDate = null;

        for (String part : parts) {
            if (part.matches("\\d{4}\\.\\d{2}\\.\\d{2}\\(.*?\\)")) {
                String dateOnly = part.replaceAll("\\(.*?\\)", "");
                if (startDate == null) {
                    startDate = LocalDate.parse(dateOnly, DOT_FORMATTER);
                } else {
                    endDate = LocalDate.parse(dateOnly, DOT_FORMATTER);
                }
            }
        }

        return new LocalDate[]{startDate, endDate};
    }

    /// [Util] Extract Reward by Airdrop Notice
    private String[] extractRewardFromString(String text) {
        Pattern pattern = Pattern.compile("(\\d{1,3}(,\\d{3})*|\\d+)\\s*(\\S+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String number = matcher.group(1).replaceAll(",", "");
            String unit = matcher.group(3);
            return new String[]{unit, number};
        }else {
            throw new BusinessException(BotException.NOT_FOUND_REWARD_INFO);
        }
    }

    /// [Util] Extract Payment Date by Airdrop Notice
    private LocalDate extractPaymentDateFromString(String text) {
        String[] parts = text.split(":");
        String datePart = parts[1].trim();
        int bracketIndex = datePart.indexOf("(");
        String date = datePart.substring(0, bracketIndex).replace(" ", "");
        return LocalDate.parse(date, DOT_FORMATTER);
    }

    /// [Util] Extract Title From Notice
    private String extractTitleFromNotice(WebElement element) {
        // Case.1 : <P> tag title
        WebElement prevTag = element.findElement(By.xpath("preceding-sibling::p[1]"));
        List<WebElement> titleList = prevTag.findElements(By.cssSelector("strong[style='font-size: 18px;']"));

        if(!titleList.isEmpty()) {
            return formattingTitle(titleList.get(0).getText());
        }

        // Case.2 : <h3> tag title
        WebElement prevH3Tag = element.findElement(By.xpath("preceding-sibling::h3[1]"));
        List<WebElement> h3Elements = prevH3Tag.findElements(By.cssSelector("strong[style='font-size: 18px;']"));

        if (!h3Elements.isEmpty()) {
            return formattingTitle(h3Elements.get(0).getText());
        }

        throw new BusinessException(BotException.NOT_FOUND_TITLE);
    }

    /// [Util] Formatting Title
    private String formattingTitle(String title) {
        if (title.startsWith("[")) {
            return title;
        }
        // Remove leading numbering format
        String formatted = title.trim().replaceFirst("^\\d+\\.\\s*", "");

        // Wrap the cleaned title with brackets
        return "[ " + formatted.trim() + " ]";
    }

    /// [Util] Extract Event Image
    private String extractImageFromNotice(List<WebElement> pElements) {
        List<WebElement> imgElements = new ArrayList<>();

        for (int i=0; i<5; i++) {
            try {
                WebElement imgTag = pElements.get(i).findElement(By.tagName("img"));
                imgElements.add(imgTag);
            } catch (NoSuchElementException e) {

            }
        }

        if (!imgElements.isEmpty()) {
            return imgElements.get(imgElements.size()-1).getAttribute("src");
        }else {
            throw new BusinessException(BotException.NOT_FOUND_IMAGE);
        }
    }
}
