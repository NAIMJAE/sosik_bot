package com.sosikbot.service.bithumb;

import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.sosikbot.entity.Airdrop;
import com.sosikbot.mapper.AirdropMapper;
import com.sosikbot.service.apiService.BithumbApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BithumbService {

    private final BithumbCrawling crawlingService;
    private final BithumbApiService bithumbApiService;
    private final AirdropMapper airdropMapper;
    
    // [Alarm] Crawling Bithumb Airdrop Event Alarm
    public List<List<Airdrop>> crawlingAirdrop() {
        List<List<Airdrop>> crawlingList = crawlingService.bithumbAirdropCrawl();

        List<List<Airdrop>> resultAirdropList = new ArrayList<>();
        
        for (List<Airdrop> airdropList : crawlingList) {
            List<Airdrop> resultList = new ArrayList<>();

            for (Airdrop airdrop : airdropList) {
                if (airdrop.getStartDate() != null && airdrop.getEndDate() != null) {
                    List<Airdrop> duplicateAirdrop = airdropMapper.airdropsDuplicateCheck(airdrop.getExchange(), airdrop.getTitle(), airdrop.getStartDate());

                    if (duplicateAirdrop == null || duplicateAirdrop.isEmpty()) {
                        airdropMapper.insertAirdrop(airdrop);
                        resultList.add(airdrop);
                    }
                }
            }
            resultAirdropList.add(resultList);
        }
        return resultAirdropList;
    }

    // [Command] SELECT Bithumb Airdrop Event List By Date
    public String selectBtAirdrop() {
        List<Airdrop> airdropList = airdropMapper.selectAirdropsByExchange("bithumb", LocalDate.now());
        return airdropToString(airdropList);
    }

    // [Alarm] SELECT Bithumb Airdrop Event For Alarm
    public List<Airdrop> alarmBtAirdrop() {
        return airdropMapper.selectAirdropsByExchange("bithumb", LocalDate.now());
    }

    // [Alarm] SELECT Bithumb Airdrop Event Estimated Reward For Alarm
    public String alarmEstimatedReward() {
        
        // 최근 에어드랍 이벤트 평균 참가자 수
        Integer EstimatedParticipants = airdropMapper.selectAirdropsAvgOfParticipants("bithumb");

        // 아직 리워드 지급되지 않은 에어드랍 이벤트
        List<Airdrop> unpaidAirdropList = airdropMapper.selectAirdropWithUnpaidReward("bithumb");

        // 현재 이벤트 코인의 가격
        Map<String, Double> coinMap = bithumbApiService.getAllTickers();

        Pattern pattern = Pattern.compile("\\[\\s*(.*?)\\((.*?)\\)\\s*.*?\\]");
        DecimalFormat formatKRW = new DecimalFormat("#,###.##");
        DecimalFormat formatCoin = new DecimalFormat("#,###.######");

        List<String> resultString = new ArrayList<>();
        resultString.add("🗓  *빗썸 에어드랍 보상 일정* (" + LocalDate.now() + ") 🗓\n");

        // 총 리워드 / 평균 참가자수 * 가격
        for(Airdrop airdrop : unpaidAirdropList) {
            log.info("airdrop : " + airdrop);
            double EstimatedCoin = Math.ceil((double) airdrop.getTotalReward() / EstimatedParticipants * 100000) / 100000;
            double EstimatedKRW = Math.ceil(EstimatedCoin * coinMap.get(airdrop.getRewardUnit()));

            Matcher matcher = pattern.matcher(airdrop.getTitle());

            if (matcher.find()) {
                resultString.add("🎁 *" + airdrop.getPaymentDate() + "* " + matcher.group(1) + "(" + matcher.group(2) + ")");
                resultString.add("🍬 보상 : *" + formatCoin.format(EstimatedCoin) + " " + airdrop.getRewardUnit() + "* (" + formatKRW.format(EstimatedKRW) + " KRW)\n");
            }
        }

        resultString.add("‼️_보상은 예상치로 실제 지급 금액과 다를 수 있음_");
        resultString.add("‼️_원화 환산은 현재 시세를 기준으로 산정_");

        return String.join("\n", resultString);
    }

    // [Alarm, Command] Bithumb Airdrop Monthly Report
    public List<String> airdropMonthlyReport(int inputYear, int inputMon, String type) {

        List<String> resultString = new ArrayList<>();

        // 명령어로 실행
        if (type.equals("command")) {
            
            String commandImgName = "";

            // 현재 날짜
            LocalDate localDate = LocalDate.now();
            int currentYear = localDate.getYear();
            int currentMonth = localDate.getMonthValue();
            int currentDay = localDate.getDayOfMonth();

            LocalDate inputDate = LocalDate.of(inputYear, inputMon, 1);
            LocalDate currentDate = LocalDate.of(currentYear, currentMonth, 1);

            if (inputDate.isBefore(currentDate)) {
                // 과거
                resultString.add("📃 *" + inputYear + "년 " + inputMon + "월 빗썸 에어드랍 정산* 📃");
                commandImgName = "airdrop_report_" + inputYear + "." + inputMon + ".00.png";

                int[] result = crateReportData(inputYear, inputMon, commandImgName);
                if (result.length > 0) {
                    resultString.add("📌 "+ inputYear + "년 " + inputMon + "월 빗썸 에어드랍 이벤트 총" + result[0] + "건");
                    resultString.add(String.format("📌 에어드랍 총 리워드 : 약 %,d원", result[1]));
                    resultString.add(commandImgName);
    
                    return resultString;
                }else {
                    return null;
                }

            } else if (inputDate.isEqual(currentDate)) {
                // 동일
                resultString.add("📃 *" + inputYear + "년 " + inputMon + "월 "+ currentDay + "일 빗썸 에어드랍 정산* 📃\n");
                commandImgName = "airdrop_report_" + inputYear + "." + inputMon + "." + currentDay + ".png";

                int[] result = crateReportData(inputYear, inputMon, commandImgName);
                if (result.length > 0) {
                    resultString.add("📌 "+ inputYear + "년 " + inputMon + "월 빗썸 에어드랍 이벤트 " + result[0] + "건");
                    resultString.add(String.format("📌 에어드랍 총 리워드 : 약 %,d원", result[1]));
                    resultString.add(commandImgName);
    
                    return resultString;
                }else {
                    return null;
                }

            } else {
                // 미래
                return null;
            }

        }else {
            // 스케줄링으로 실행
            String ScheduledImgName = "airdrop_report_" + inputYear + "." + inputMon + ".00.png";

            resultString.add("📃 *" + inputYear + "년 " + inputMon + "월 빗썸 에어드랍 정산* 📃\n");
            
            int[] result = crateReportData(inputYear, inputMon, ScheduledImgName);
            if (result.length > 0) {
                resultString.add("📌 "+ inputYear + "년 " + inputMon + "월 빗썸 에어드랍 이벤트 " + result[0] + "건");
                resultString.add(String.format("📌 에어드랍 총 리워드 : 약 %,d원", result[1]));
                resultString.add(ScheduledImgName);
    
                return resultString;
            }else {
                return null;
            }
        }
    }

    // Create Report Data For Airdrop Monthly Report 
    public int[] crateReportData(int year, int month, String imgName) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        // DB 조회
        List<Airdrop> monthlyAirdropList = airdropMapper.selectAirdropForMonthlyReport("Bithumb", firstDay.toString(), lastDay.toString());

        if (monthlyAirdropList.isEmpty()) {
            return new int[0];
        }

        double totalReward = 0;

        // 테이블 데이터 생성
        String[] headers = {"DATE", "COIN", "REWARD", "KRW"};
        String[][] tableData = new String[monthlyAirdropList.size() + 1][4];

        int rowIndex = 0;
        for (Airdrop airdrop : monthlyAirdropList) {
            int mon = airdrop.getPaymentDate().getMonthValue();
            int day = airdrop.getPaymentDate().getDayOfMonth();
            double coin = airdrop.getActualReward_coin();
            String name = airdrop.getCoin();
            double krw = airdrop.getActualReward_krw();

            // 데이터 저장
            tableData[rowIndex][0] = String.format("%02d.%02d", mon, day);
            tableData[rowIndex][1] = name;
            tableData[rowIndex][2] = String.format("%.4f", coin);
            tableData[rowIndex][3] = String.format("%,d KRW", (int) krw);

            totalReward += krw;
            rowIndex++;
        }

        // 총합 추가
        tableData[rowIndex][0] = "TOTAL";
        tableData[rowIndex][1] = "-";
        tableData[rowIndex][2] = "-";
        tableData[rowIndex][3] = String.format("%,d KRW", (int) totalReward);

        // 이미지로 저장
        createTableImage(headers, tableData, imgName);

        int[] result = {rowIndex, (int) totalReward};

        return result;
    }

    // Create Table Image For Airdrop Monthly Report 
    public void createTableImage(String[] headers, String[][] data, String outputFileName) {
        // FILE Dir 생성
        String currentDir = Paths.get("").toAbsolutePath().toString();
        File imgDir = new File(currentDir, "report");
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }

        // 셀 크기 및 간격 설정
        int cellWidth = 200;       // 셀 너비
        int cellHeight = 40;       // 셀 높이
        int padding = 20;          // 이미지 패딩
        int textPadding = 20;      // 셀 내부 패딩
        int columns = headers.length;
        int rows = data.length + 1; // 헤더 포함

        // 이미지 크기 계산
        int imageWidth = cellWidth * columns + 2 * padding;
        int imageHeight = cellHeight * rows + 2 * padding;

        // 이미지 생성
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 배경 설정
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imageWidth, imageHeight);

        // 글꼴 및 텍스트 설정
        Font font = new Font("SansSerif", Font.PLAIN, 16);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        // 테이블 그리기
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int x = padding + col * cellWidth;
                int y = padding + row * cellHeight;

                // 셀 배경색
                if (row == 0) {
                    g2d.setColor(Color.LIGHT_GRAY); // 헤더 배경
                } else {
                    g2d.setColor(Color.WHITE); // 데이터 배경
                }
                g2d.fillRect(x, y, cellWidth, cellHeight);

                // 셀 테두리
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, cellWidth, cellHeight);

                // 텍스트 출력
                String text;
                if (row == 0) {
                    text = headers[col];
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                } else {
                    text = data[row - 1][col];
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
                }
                FontMetrics metrics = g2d.getFontMetrics();
                int textWidth = metrics.stringWidth(text);
                int textHeight = metrics.getHeight();

                // 텍스트 위치 계산 (패딩 적용)
                int textX;
                if (row == 0) { // 첫 번째 행(헤더)는 항상 가운데 정렬
                    textX = x + (cellWidth - textWidth) / 2;
                } else if (col == 0 || col == 1) { // 첫 번째, 두 번째 열: 가운데 정렬
                    textX = x + (cellWidth - textWidth) / 2;
                } else { // 세 번째, 네 번째 열: 오른쪽 정렬
                    textX = x + cellWidth - textWidth - textPadding;
                }
                int textY = y + (cellHeight - textHeight) / 2 + metrics.getAscent();

                g2d.setColor(Color.BLACK);
                g2d.drawString(text, textX, textY);
            }
        }

        g2d.dispose();

        // 이미지 저장
        try {
            File outputFile = new File(imgDir, outputFileName);
            ImageIO.write(image, "png", outputFile);
            System.out.println("표 이미지 생성 완료: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // [Command] Register Reward Of Airdrop Event
    public String registerAirdropReward(String coinName, String rewardCoin, String rewardKRW) {
        
        Airdrop existingAirdrop = airdropMapper.selectAirdropByRegister("Bithumb", coinName, LocalDate.now());
        if (existingAirdrop == null) {
            return "해당 코인의 이벤트를 찾을 수 없습니다.";
        }

        double actual_coin = Double.parseDouble(rewardCoin);
        int actual_krw = (int) Double.parseDouble(rewardKRW);
        
        existingAirdrop.setActualRward(actual_coin, actual_krw);

        airdropMapper.updateRewardOfAirdrop(existingAirdrop);

        int recentAverage = airdropMapper.selectAirdropsAvgOfParticipants("Bithumb");
        double calculatedAverage = existingAirdrop.getTotalReward() / actual_coin;

        recentAverage = (int) (Math.ceil(recentAverage / 100.0) * 100);
        int roundedAverage = (int) (Math.ceil(calculatedAverage / 100.0) * 100);    
        
        DecimalFormat commaFormatter = new DecimalFormat("#,###");
        String formattedRecentAverage = commaFormatter.format(recentAverage);
        String formattedThisAverage = commaFormatter.format(roundedAverage);

        List<String> resultString = new ArrayList<>();
        resultString.add("💰 *빗썸 " + coinName + " 에어드랍 보상 지급* 💰");
        resultString.add("- 보상 : 약 " + actual_coin + " " + coinName + " (" + commaFormatter.format(actual_krw) + " KRW)\n");
        resultString.add("- 최근 이벤트 참여자 평균 : 약 " + formattedRecentAverage);
        resultString.add("- 이번 이벤트 참여자 추정 : 약 " + formattedThisAverage);

        return String.join("\n", resultString);
    }

    // [ToString] Airdrop Event List ToString For New Event Alarm
    public String airdropToString(List<Airdrop> airdropList) {
        List<String> resultString = new ArrayList<>();

        if (!airdropList.isEmpty()) {
            resultString.add("🔔 *Bithumb 새로운 에어드랍 이벤트* 🔔\n");
            for (Airdrop item : airdropList) {
                if(item.getStartDate() != null && item.getEndDate() != null) {
                    resultString.add("🎁 *" + item.getTitle() + "*");
                    resultString.add("🍬 기간 : " + item.getStartDate() + " ~ " + item.getEndDate());
                    resultString.add("🍬 " + item.getContent() + "\n");
                }
            }
        }else {
            resultString.add("💣 현재 진행중인 Airdrop 이벤트가 없습니다. 💥");
        }
        return String.join("\n", resultString);
    }

    // [ToString] Airdrop Event List ToString For Every Day Alarm
    public String alarmAirdropToString(List<Airdrop> airdropList) {
        List<String> resultString = new ArrayList<>();
        resultString.add("🔔 *Bithumb 에어드랍 이벤트 알림* 🔔\n");

        if (!airdropList.isEmpty()) {
            for (Airdrop item : airdropList) {
                if(item.getStartDate() != null && item.getEndDate() != null) {

                    resultString.add("🎁 *" + item.getTitle() + "*");
                    resultString.add("🍬 기간 : " + item.getStartDate() + " ~ " + item.getEndDate());
                    resultString.add("🍬 " + item.getContent() + "\n");
                }
            }
            return String.join("\n", resultString);

        }else {
            return null;
        }
    }
}
