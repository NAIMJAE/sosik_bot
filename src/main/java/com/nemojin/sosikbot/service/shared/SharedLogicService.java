package com.nemojin.sosikbot.service.shared;

import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.mapper.SharedMapper;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.Estimate;
import com.nemojin.sosikbot.model.Participant;
import com.nemojin.sosikbot.service.apiService.TickerService;
import com.nemojin.sosikbot.util.DateParser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;
@Service
@RequiredArgsConstructor
public class SharedLogicService {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final SharedMapper sharedMapper;
    private final TickerService tickerService;

    /// [Function] Calculate Estimated Rewards for Unpaid Airdrops
    public Map<String, List<Estimate>> calculateEstimatedAirdropRewards() {
        // Retrieve unpaid airdrops
        List<Airdrop> unpaidList = sharedMapper.selectUnpaidAirdrops();
        if (unpaidList.isEmpty()) {return null;}

        Map<String, List<Airdrop>> unpaidMap = new HashMap<>();
        for (Airdrop each : unpaidList) {
            if (!unpaidMap.containsKey(each.getExchange())) {
                unpaidMap.put(each.getExchange(), new ArrayList<>());
            }
            unpaidMap.get(each.getExchange()).add(each);
        }

        // Retrieve airdrops avg of participants
        List<Participant> avgList = sharedMapper.selectAirdropsAvgOfParticipants();

        Map<String, Integer> avgMap = new HashMap<>();
        for (Participant each : avgList) {
            avgMap.put(each.getExchange(), each.getAverage());
        }

        // Coin current price by exchange
        Map<String, Map<String, Double>> exchangeMap = new HashMap<>();
        for (String exchange : unpaidMap.keySet()) {
            switch (exchange) {
                case "Bithumb":
                    exchangeMap.put(exchange, tickerService.getAllBithumbTickers());
                    break;
                default:
                    throw new BusinessException(BotException.NOT_FOUND_EXCHANGE_TICKER);
            }
        }

        // Calculate estimated rewards
        Map<String, List<Estimate>> resultMap = new HashMap<>();

        for (Map.Entry<String, List<Airdrop>> entry : unpaidMap.entrySet()) {
            String exchange = entry.getKey();
            List<Airdrop> airdropList = entry.getValue();

            Integer avg = avgMap.get(exchange);
            if (avg == null) {continue;}
            Map<String, Double> coinMap = exchangeMap.get(exchange);

            for (Airdrop each : airdropList) {
                double EstimatedCoin = Math.ceil((double) each.getTotalReward() / avg * 100000) / 100000;
                double EstimatedKRW = Math.ceil(EstimatedCoin * coinMap.getOrDefault(each.getRewardUnit(), 0.0));

                if (!resultMap.containsKey(exchange)) {
                    resultMap.put(exchange, new ArrayList<>());
                }
                resultMap.get(exchange).add(new Estimate(each, EstimatedCoin, EstimatedKRW));
            }
        }

        return resultMap;
    }

    /// [Function] Calculate Recent Average Participants
    public Map<String, LinkedHashMap<String, Integer>> CalculateRecentAverageParticipants() {
        LocalDate date = LocalDate.now().minusMonths(2).withDayOfMonth(1);
        List<Participant> avgList = sharedMapper.selectRecentAvgOfParticipants(date);

        Map<String, LinkedHashMap<String, Integer>> resultMap = new HashMap<>();
        int count = 0;

        for (Participant each : avgList) {

            String exchange = each.getExchange();
            LocalDate paymentDate = each.getPaymentDate();
            String month = paymentDate.getYear() + "-" + String.format("%02d", paymentDate.getMonthValue());
            int average = each.getAverage();

            if(!resultMap.containsKey(exchange)) {
                resultMap.put(exchange, new LinkedHashMap<>());
            }

            LinkedHashMap<String, Integer> monthMap = resultMap.get(exchange);

            if (monthMap.containsKey(month)) {
                int current = monthMap.get(month) * count;
                count++;

                monthMap.put(month, (current + average)/count);
            } else {
                count = 1;
                monthMap.put(month, average);
            }
        }

        return resultMap;
    }

    /// [Function] Calculate Month Average Participants
    public Map<String, List<String>> CalculateMonthAverageParticipants() {
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);
        List<Participant> avgList = sharedMapper.selectMonthAvgOfParticipants(firstDay.toString(), lastDay.toString());

        DecimalFormat formatAvg = new DecimalFormat("#,###.##");

        Map<String, List<String>> resultMap = new HashMap<>();

        for (Participant each : avgList) {
            String exchange = each.getExchange();
            String paymentDate = DateParser.shortStyleDate(each.getPaymentDate());

            if(!resultMap.containsKey(exchange)) {
                resultMap.put(exchange, new ArrayList<>());
            }

            String text = paymentDate + " - " + each.getCoin() + " : " + formatAvg.format(each.getAverage()) + "명";
            resultMap.get(exchange).add(text);
        }

        return resultMap;
    }

    /// [Function] Create Report Data for Airdrop Monthly Report
    public int[] createAirdropForMonthlyReport(int inputYear, int inputMon, String imageName) {
        // set date range for report
        LocalDate firstDay = LocalDate.of(inputYear, inputMon, 1);
        YearMonth yearMonth = YearMonth.of(inputYear, inputMon);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        List<Airdrop> airdropList = sharedMapper.selectAirdropsForMonthlyReport(firstDay.toString(), lastDay.toString());

        // build 2D table data
        return createReportTable(airdropList, imageName);
    }

    /// [Util] Converts Airdrop List into 2D Table Data and Generates Report Image
    private int[] createReportTable(List<Airdrop> airdropList, String imageName) {
        // initialize 2D array to store table content
        String[][] tableData = new String[airdropList.size() + 1][5];

        int totalReward = 0;
        int rowIndex = 0;

        for (Airdrop each : airdropList) {
            int mon = each.getPaymentDate().getMonthValue();
            int day = each.getPaymentDate().getDayOfMonth();
            double coin = each.getActualReward_coin();
            Integer krw = each.getActualReward_krw();

            tableData[rowIndex][0] = String.format("%02d.%02d", mon, day);
            tableData[rowIndex][1] = each.getExchange();
            tableData[rowIndex][2] = each.getCoin();
            tableData[rowIndex][3] = String.format("%.4f", coin);
            tableData[rowIndex][4] = String.format("%,d KRW", krw);

            totalReward += krw;
            rowIndex++;
        }

        tableData[rowIndex][0] = "TOTAL";
        tableData[rowIndex][1] = "-";
        tableData[rowIndex][2] = "-";
        tableData[rowIndex][3] = "-";
        tableData[rowIndex][4] = String.format("%,d KRW", totalReward);

        // generate PNG image using table data
        createReportImage(tableData, imageName);

        return new int[]{rowIndex, totalReward};
    }

    /// [Util] Draws Airdrop Report Table into PNG Image and Saves It to File
    private void createReportImage(String[][] data, String imageName) {
        // FILE Dir 생성
        String currentDir = Paths.get("").toAbsolutePath().toString();
        File imgDir = new File(currentDir, "report");
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }

        String[] headers = {"DATE", "EXCHANGE", "COIN", "REWARD", "KRW"};

        // configure cell size and spacing
        int cellWidth = 200;
        int cellHeight = 40;
        int padding = 20;
        int textPadding = 20;
        int columns = headers.length;
        int rows = data.length + 1;

        // calculate image dimensions
        int imageWidth = cellWidth * columns + 2 * padding;
        int imageHeight = cellHeight * rows + 2 * padding;

        // create image canvas
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // fill background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imageWidth, imageHeight);

        // set font and text color
        Font font = new Font("SansSerif", Font.PLAIN, 16);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        // draw table (cells, borders, text)
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int x = padding + col * cellWidth;
                int y = padding + row * cellHeight;

                // set cell background
                if (row == 0) {
                    g2d.setColor(Color.LIGHT_GRAY);
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.fillRect(x, y, cellWidth, cellHeight);

                // draw cell border
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, cellWidth, cellHeight);

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

                // calculate text position (with padding)
                int textX;
                if (row == 0) {
                    textX = x + (cellWidth - textWidth) / 2;
                } else if (col == 0 || col == 1) {
                    textX = x + (cellWidth - textWidth) / 2;
                } else {
                    textX = x + cellWidth - textWidth - textPadding;
                }
                int textY = y + (cellHeight - textHeight) / 2 + metrics.getAscent();

                g2d.setColor(Color.BLACK);
                g2d.drawString(text, textX, textY);
            }
        }
        g2d.dispose();

        // save image file
        try {
            File outputFile = new File(imgDir, imageName);
            ImageIO.write(image, "png", outputFile);
            logger.info("CREATE REPORT IMAGE SUCCESS : " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            throw new BusinessException(BotException.CREATE_REPORT_IMAGE_FAIL);
        }
    }
}
