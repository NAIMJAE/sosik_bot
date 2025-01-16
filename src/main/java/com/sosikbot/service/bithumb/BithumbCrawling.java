package com.sosikbot.service.bithumb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import com.sosikbot.config.WebDriverConfig;
import com.sosikbot.entity.Airdrop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class BithumbCrawling {

    private final WebDriverConfig webDriver;

    public List<List<Airdrop>> bithumbAirdropCrawl() {
        // WebDriver 객체 생성
        WebDriver driver = webDriver.getDriver();

        // FILE Dir 생성
        String currentDir = Paths.get("").toAbsolutePath().toString();
        File imgDir = new File(currentDir, "img");
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }

        // Bithumb 공지사항 크롤링 시작
        try {
            driver.get("https://feed.bithumb.com/notice");

            List<WebElement> noticeItems = driver.findElements(By.cssSelector(".NoticeContentList_notice-list__i337r li"));
            List<List<Airdrop>> resultList = new ArrayList<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.d");

            for (WebElement item : noticeItems) {
                String noticeTitle = item.findElement(By.cssSelector(".NoticeContentList_notice-list__link-title__nlmSC")).getText();
                String link = item.findElement(By.tagName("a")).getAttribute("href");

                // "에어드랍 이벤트" 이 포함된 제목 필터링
                if (noticeTitle.contains("에어드랍 이벤트")) {
                    List<Airdrop> airdropList = new ArrayList<>();

                    WebDriver driver2 = webDriver.getDriver();

                    try {
                        driver2.get(link);

                        // 제목, 내용, 이미지 추출
                        WebElement noticeDetailDiv = driver2.findElement(By.cssSelector(".NoticeDetailContent_detail-content__notice__X5VPQ"));
                        List<WebElement> pElements = noticeDetailDiv.findElements(By.tagName("p"));
                        List<WebElement> ulElements = noticeDetailDiv.findElements(By.cssSelector("ul[style='list-style-type: square;']"));

                        // 개별 ul
                        for (int i=0; i<ulElements.size()-1; i++) {
                            List<WebElement> liElements = ulElements.get(i).findElements(By.tagName("li"));
                            if (liElements.size() > 4) {
                                String title = null;

                                WebElement prevTag = ulElements.get(i).findElement(By.xpath("preceding-sibling::p[1]"));
                                List<WebElement> titleList = prevTag.findElements(By.cssSelector("strong[style='font-size: 18px;']"));

                                if (titleList.isEmpty()) {
                                    WebElement prevH3Tag = ulElements.get(i).findElement(By.xpath("preceding-sibling::h3[1]"));
                                    List<WebElement> h3Elements = prevH3Tag.findElements(By.cssSelector("strong[style='font-size: 18px;']"));

                                    if (!h3Elements.isEmpty()) {
                                        title = h3Elements.get(0).getText();
                                    }else {
                                        continue;
                                    }
                                }else {
                                    title = titleList.get(0).getText();
                                }

                                String coin = title.replaceAll(".*\\(([^)]*)\\).*", "$1");

                                LocalDate startDate = null;
                                LocalDate endDate = null;
                                String content = null;
                                int totalReward = 0;
                                String rewardUnit = null;
                                LocalDate paymentDate = null;

                                for (WebElement li : liElements) {
                                    String text = li.getText();
    
                                    if (text.contains("기간 : ")) {
                                        LocalDate[] dates = parseDates(text, formatter);
                                        startDate = dates[0];
                                        endDate = dates[1];
    
                                    }else if (li.getText().contains("대상 : ")) {
                                        content = text;
    
                                    }else if (li.getText().contains("보상 : ")) {
                                        String rewardInfo = parseRewards(text);
                                        String[] sptInfo = rewardInfo.split("_");
                                        totalReward = Integer.parseInt(sptInfo[0]);
                                        rewardUnit = sptInfo[1];
    
                                    }else if (li.getText().contains("이벤트 지급일 : ")) {
                                        paymentDate = parsePaymentDate(text, formatter);
    
                                    }
                                }

                                if (startDate != null && endDate != null) {
                                    // 이미지 처리
                                    boolean imgResult = selectImgtag(imgDir, pElements, title);
                                    if (imgResult) {
                                        Airdrop airdrop = new Airdrop(coin, "Bithumb", title, content, startDate, endDate, paymentDate, totalReward, rewardUnit, link);
                                        airdropList.add(airdrop);
                                    }
                                }
                            }else {
                                continue;
                            }
                        }

                    } catch(Exception e) {
                        driver2.quit();
                        log.error(e.getMessage(), e);
                    } finally {
                        driver2.quit();
                    }
                    if (airdropList.size() > 0) {
                        resultList.add(airdropList);
                    }
                }
            }
            return resultList;
        } catch(Exception e) {
            driver.quit();
            log.error(e.getMessage(), e);

            return null;
        } finally {
            driver.quit();
        }
    }

    // 날짜 추출
    private LocalDate[] parseDates(String text, DateTimeFormatter formatter) {
        String[] parts = text.split("\\s+");
        LocalDate startDate = null;
        LocalDate endDate = null;
    
        for (String part : parts) {
            if (part.matches("\\d{4}\\.\\d{2}\\.\\d{2}\\(.*?\\)")) {
                String dateOnly = part.replaceAll("\\(.*?\\)", "");
                if (startDate == null) {
                    startDate = LocalDate.parse(dateOnly, formatter);
                } else {
                    endDate = LocalDate.parse(dateOnly, formatter);
                }
            }
        }
    
        return new LocalDate[]{startDate, endDate};
    }

    // 보상 추출
    private String parseRewards(String text) {
        Pattern pattern = Pattern.compile("(\\d{1,3}(,\\d{3})*|\\d+)\\s*([A-Za-z]+)");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String number = matcher.group(1).replaceAll(",", "");
            String unit = matcher.group(3);
            return number + "_" + unit;
        }else {
            return null;
        }
    }

    // 이벤트 지급일
    private LocalDate parsePaymentDate(String text, DateTimeFormatter formatter) {
        String[] parts = text.split(":");
        String datePart = parts[1].trim();
        int bracketIndex = datePart.indexOf("(");
        String date = datePart.substring(0, bracketIndex).replace(" ", "");
        return LocalDate.parse(date, formatter);
    }

    // 이미지 선택 메서드
    private boolean selectImgtag(File imgDir, List<WebElement> pElements, String title) {
        List<WebElement> imgElements = new ArrayList<>();

        for (int i=0; i<5; i++) {
            try {
                WebElement imgTag = pElements.get(i).findElement(By.tagName("img"));
                if (imgTag != null) {
                    imgElements.add(imgTag);
                }
            } catch (NoSuchElementException e) {
                System.out.println("No <img> tag found in <p> tag at index " + i);
            }
        }

        String imgUrl = imgElements.get(imgElements.size()-1).getAttribute("src"); 

        if (imgUrl != null && imgUrl.startsWith("http")) {
            String fileName = title + ".png";
            File outputFile = new File(imgDir, fileName);
            if (outputFile.exists()) {
                System.out.println("이미 존재하는 파일: " + outputFile.getAbsolutePath());
                return false;
            }else {
                downloadImage(imgUrl, outputFile);
                return true;
            }
        }else {
            return false;
        }
    }

    // 이미지 다운로드 메서드
    public static void downloadImage(String imageUrl, File outputFile) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 이미지 데이터를 가져옴
                InputStream inputStream = connection.getInputStream();

                // 파일에 이미지 저장
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Saved: " + outputFile.getAbsolutePath());
                }
                inputStream.close();
            } else {
                System.out.println("Failed to download: " + imageUrl + " - Response code: " + connection.getResponseCode());
            }

        } catch (Exception e) {
            System.out.println("Error downloading image: " + imageUrl);
            e.printStackTrace();
        }
    }
}
