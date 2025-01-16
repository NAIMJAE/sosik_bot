package com.sosikbot.service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.sosikbot.config.WebDriverConfig;
import com.sosikbot.entity.Airdrop;
import com.sosikbot.entity.LaunchPool;
import com.sosikbot.entity.PoolDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrawlingService {
    //////////////////////////////////////////이제 이거 안씀////////////////////////////////////////////////////////
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
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.d"); // 날짜 변환을 위한 포맷터

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
                        List<WebElement> h3Elements = noticeDetailDiv.findElements(By.cssSelector("strong[style='font-size: 18px;']"));
                        List<WebElement> pElements = noticeDetailDiv.findElements(By.tagName("p"));

                        
                        // 제목, 내용 처리
                        List<WebElement> ulElements = noticeDetailDiv.findElements(By.cssSelector("ul[style='list-style-type: square;']"));
                        int titleIdx = 0;

                        // 개별 ul
                        for (int i=0; i<ulElements.size()-1; i++) {
                            List<WebElement> liElements = ulElements.get(i).findElements(By.tagName("li"));
                            if (liElements.size() < 4) {
                                continue;
                            }
                            String title = h3Elements.get(titleIdx++).getText();
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
                            // 이미지 처리
                            boolean imgResult = selectImgtag(imgDir, pElements, title);

                            if (imgResult && startDate != null && endDate != null) {
                                Airdrop airdrop = new Airdrop(coin, "Bithumb", title, content, startDate, endDate, paymentDate, totalReward, rewardUnit, link);
                                log.info("new airdrop : " + airdrop);
                                airdropList.add(airdrop);
                            }

                        }

                        // 로그 통일 시키기
                        // 현재 로직 문제 없는지 확인
                        // 보상에서 코인 이름 추출해내기
                    } catch(Exception e) {
                        driver2.quit();
                        log.error(e.getMessage(), e);
                    } finally {
                        driver2.quit();
                    }
                    resultList.add(airdropList);
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

    public Map<LaunchPool, List<PoolDetail>> bybitLaunchpoolCrawl(List<LaunchPool> launchPoolLast3) {
        // System.setProperty("webdriver.chrome.driver", "C:/Users/devimjae/Desktop/chromedriver-win64/chromedriver.exe");
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
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".NoEndActivites_contentOut__f5swu")));

            //List<WebElement> noticeItems = driver.findElements(By.cssSelector(".EndActivites_activity__ivUjF"));
            List<WebElement> noticeItems = driver.findElements(By.cssSelector(".NoEndActivites_contentOut__f5swu"));

            Map<LaunchPool, List<PoolDetail>> result = new HashMap<>();

            outerLoop:
            for (WebElement item : noticeItems) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", item);
                Thread.sleep(500);

                String title = item.findElement(By.cssSelector(".ActivityCardItem_prizeCoin__E8_0o")).getText();
                //String status = item.findElement(By.cssSelector(".ActivityCardItem_completed__RJoI5")).getText();
                String status = item.findElement(By.cssSelector(".ActivityCardItem_timeTop__e4rRr")).getText();
                String date = item.findElement(By.cssSelector(".ActivityCardItem_time__DbRtS")).getText();
                List<WebElement> btnList = item.findElements(By.cssSelector(".ActivityCardItem_tagText__JfIrg"));
                
                LocalDateTime[] dateArr = extractDateTimeForBybit(date);

                for (LaunchPool last3 : launchPoolLast3) {
                    if (last3.getTitle().equals(title) && last3.getStartDate().equals(dateArr[0])) {
                        continue outerLoop;
                    }
                }

                if (status.contains("Staking Starts In")) {
                    status = "Ready";
                }else {
                    status = null;
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
                        WebElement newTabElement = null;

                        try {
                            newTabElement = driver.findElement(By.cssSelector(".article-detail-content"));
                        }catch(Exception e) {
                            e.getMessage();
                            log.info("캐치캐치" );
                            continue outerLoop;
                        }


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
            log.error("에러 발생: {}", e.getMessage(), e);

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

        // 시작 날짜가 종료 날짜보다 뒤에 있으면 연도를 조정
        if (startDateTime.isAfter(endDateTime)) {
            endDateTime = endDateTime.plusYears(1);
        }

        return new LocalDateTime[]{startDateTime, endDateTime};
    }

    // 보상 추출
    private String parseRewards(String text) {
        Pattern pattern = Pattern.compile("(\\d{1,3}(,\\d{3})*|\\d+)\\s*([A-Za-z]+)");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String number = matcher.group(1).replaceAll(",", "");
            String unit = matcher.group(3);
            System.out.println(number + " " + unit);
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
