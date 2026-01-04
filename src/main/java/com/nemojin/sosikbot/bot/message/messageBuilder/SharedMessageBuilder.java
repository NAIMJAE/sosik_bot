package com.nemojin.sosikbot.bot.message.messageBuilder;

import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.Estimate;
import com.nemojin.sosikbot.model.LaunchPool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class SharedMessageBuilder {

    /// [Function] Build Available Airdrop Message
    public SendMessage buildAvailableAirdropMessage(Map<String, List<Airdrop>> airdropMap) {
        SendMessage message = new SendMessage();
        message.disableWebPagePreview();
        message.setParseMode("Markdown");
        message.setText(buildTxtForAvailableAirdrop(airdropMap));
        message.setReplyMarkup(buildBtnForAvailableAirdrop(airdropMap));

        return message;
    }

    /// Build Text for Available Airdrop
    private String buildTxtForAvailableAirdrop(Map<String, List<Airdrop>> airdropMap) {
        List<String> resultString = new ArrayList<>();
        resultString.add("🔔 *" + LocalDate.now() + " 에어드랍 이벤트 알림* 🔔\n");

        for (Map.Entry<String, List<Airdrop>> entry : airdropMap.entrySet()) {
            String exchange = entry.getKey();
            List<Airdrop> airdropList = entry.getValue();

            resultString.add("*[" + exchange + "]*");
            for (Airdrop each : airdropList) {
                resultString.add("🎁 *" + each.getTitle() + "*");
                resultString.add("🍬 기간 : " + each.getStartDate() + " ~ " + each.getEndDate());
                resultString.add("🍬 " + each.getContent() + "\n");
            }
        }
        return String.join("\n", resultString);
    }

    /// Build Inline Keyboard Button for Available Airdrop
    private InlineKeyboardMarkup buildBtnForAvailableAirdrop(Map<String, List<Airdrop>> airdropMap) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (Map.Entry<String, List<Airdrop>> entry : airdropMap.entrySet()) {
            String exchange = entry.getKey();
            List<Airdrop> airdropList = entry.getValue();

            String url = getBtnUrlByExchange(exchange);

            for (Airdrop airdrop : airdropList) {
                InlineKeyboardButton transactionButton = new InlineKeyboardButton();
                transactionButton.setText("💰" + airdrop.getCoin() + " 바로가기");
                transactionButton.setUrl(url + airdrop.getCoin() + "-KRW");
                row.add(transactionButton);

                if (row.size() == 2) {
                    rowsInline.add(row);
                    row = new ArrayList<>();
                }
            }
        }

        rowsInline.add(row);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    /// Return Trade URL Prefix Based on Exchange Name
    private String getBtnUrlByExchange(String exchange) {
        switch (exchange) {
            case "Bithumb" :
                return "https://www.bithumb.com/react/trade/order/";
            default:
                throw new BusinessException(BotException.NOT_FOUND_EXCHANGE);
        }
    }

    /// [Function] Build Available LaunchPool Message
    public SendMessage buildAvailableLaunchPoolMessage(Map<String, List<LaunchPool>> launchpoolMap) {
        SendMessage message = new SendMessage();
        message.setParseMode("Markdown");
        message.setText(buildTxtForAvailableLaunchPool(launchpoolMap));

        return message;
    }

    /// Build Text for Available LaunchPool
    private String buildTxtForAvailableLaunchPool(Map<String, List<LaunchPool>> launchpoolMap) {
        List<String> resultString = new ArrayList<>();
        resultString.add("🔔 *런치풀 이벤트 알림* 🔔\n");

        for (Map.Entry<String, List<LaunchPool>> entry : launchpoolMap.entrySet()) {
            String exchange = entry.getKey();
            List<LaunchPool> launchPoolList = entry.getValue();

            resultString.add("*[" + exchange + "]*");
            for (LaunchPool each : launchPoolList) {
                resultString.add("🎁 *" + each.getTitle() + "*");
                resultString.add("🍬 기간 : " + each.getStartDate() + " ~ " + each.getEndDate());
            }
        }

        return String.join("\n", resultString);
    }

    /// [Function] Build Estimated Rewards Message
    public SendMessage buildEstimatedRewardsMessage(Map<String, List<Estimate>> EstimatedMap) {
        SendMessage message = new SendMessage();
        message.setParseMode("Markdown");
        message.setText(buildTxtForEstimatedRewards(EstimatedMap));

        return message;
    }

    /// Build Message Text for Estimated Rewards
    private String buildTxtForEstimatedRewards(Map<String, List<Estimate>> EstimatedMap) {
        Pattern pattern = Pattern.compile("\\[\\s*(.*?)\\((.*?)\\)\\s*.*?\\]");
        DecimalFormat formatKRW = new DecimalFormat("#,###.##");
        DecimalFormat formatCoin = new DecimalFormat("#,###.######");

        List<String> resultString = new ArrayList<>();
        resultString.add("🗓  *에어드랍 보상 일정* (" + LocalDate.now() + ") 🗓\n");

        for (Map.Entry<String, List<Estimate>> entry : EstimatedMap.entrySet()) {
            String exchange = entry.getKey();
            List<Estimate> EstimatedList = entry.getValue();

            resultString.add("*[" + exchange + "]*");
            for (Estimate each : EstimatedList) {
                Matcher matcher = pattern.matcher(each.getAirdrop().getTitle());

                if (matcher.find()) {
                    resultString.add("🎁 *" + each.getAirdrop().getPaymentDate() + "* " + matcher.group(1) + "(" + matcher.group(2) + ")");

                    if (each.getAirdrop().getType().equals("Limit")) {
                        resultString.add("🧪 보상 예측 불가능\n");
                    }else {
                        resultString.add("🍬 보상 : *" + formatCoin.format(each.getCoin()) + " " + each.getAirdrop().getRewardUnit() + "* (" + formatKRW.format(each.getKrw()) + " KRW)\n");
                    }
                }
            }
        }
        resultString.add("‼️_메이커(Maker) 거래 이벤트 보상 예측은 불가능_");
        resultString.add("‼️_보상은 예상치로 실제 지급 금액과 다를 수 있음_");
        resultString.add("‼️_원화 환산은 현재 시세를 기준으로 산정_");

        return String.join("\n", resultString);
    }

    /// [Function] Build Monthly Airdrop Report Message
    public SendPhoto buildMonthlyAirdropReportMessage(int year, int month, int totalCount, int totalReward, String imageName) {
        SendPhoto message = new SendPhoto();
        message.setParseMode("Markdown");
        message.setCaption(buildTxtForMonthlyAirdropReport(year, month, totalCount, totalReward));
        message.setPhoto(new InputFile(buildImgForMonthlyAirdropReport(imageName)));

        return message;
    }

    /// Build Text for Monthly Airdrop Report
    private String buildTxtForMonthlyAirdropReport(int year, int month, int totalCount, int totalReward) {
        List<String> resultString = new ArrayList<>();

        resultString.add("📃 *" + year + "년 " + month + "월 에어드랍 정산* 📃\n");
        resultString.add("📌 "+ year + "년 " + month + "월 에어드랍 이벤트 " + totalCount + "건");
        resultString.add(String.format("📌 에어드랍 총 리워드 : 약 %,d원", totalReward));

        return String.join("\n", resultString);
    }

    /// Retrieve Image File Path for Monthly Airdrop Report
    private File buildImgForMonthlyAirdropReport(String fileName) {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path imagePath = Paths.get(currentDir, "report", fileName);
        return imagePath.toFile();
    }

    /// [Function] Build Recent Average Message
    public SendMessage buildRecentAverageMessage(Map<String, LinkedHashMap<String, Integer>> avgMap) {
        SendMessage message = new SendMessage();
        message.setParseMode("Markdown");
        message.setText(buildTxtForRecentAverage(avgMap));

        return message;
    }

    /// Build Text for Recent Average
    private String buildTxtForRecentAverage(Map<String, LinkedHashMap<String, Integer>> avgMap) {
        DecimalFormat formatAvg = new DecimalFormat("#,###.##");

        List<String> resultString = new ArrayList<>();
        resultString.add("🗓  *에어드랍 참가자 추이*  🗓\n");

        for (Map.Entry<String, LinkedHashMap<String, Integer>> entry : avgMap.entrySet()) {
            String exchange = entry.getKey();
            Map<String, Integer> monthMap = entry.getValue();

            resultString.add("*[" + exchange + "]*");
            for(Map.Entry<String, Integer> each : monthMap.entrySet()) {
                String month = each.getKey();
                Integer average = each.getValue();

                resultString.add(month + " : 평균 " + formatAvg.format(average) + "명");
            }
            resultString.add(" ");
        }
        return String.join("\n", resultString);
    }

    /// [Function] Build Month Average Message
    public SendMessage buildMonthAverageMessage(Map<String, List<String>> avgMap) {
        SendMessage message = new SendMessage();
        message.setParseMode("Markdown");
        message.setText(buildTxtForMonthAverage(avgMap));

        return message;
    }

    /// Build Text for Recent Average
    private String buildTxtForMonthAverage(Map<String, List<String>> avgMap) {
        List<String> resultString = new ArrayList<>();
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        resultString.add("🗓  * " + year + "." + month + " 에어드랍 참가자 추이*  🗓\n");

        for (Map.Entry<String, List<String>> entry : avgMap.entrySet()) {
            String exchange = entry.getKey();
            List<String> eventList = entry.getValue();

            resultString.add("*[" + exchange + "]*");
            resultString.addAll(eventList);
            resultString.add(" ");
        }
        return String.join("\n", resultString);
    }

    /// [Function] Build Reward Notification Message
    public SendMessage buildRewardNotificationMessage(List<Airdrop> airdropList) {
        SendMessage message = new SendMessage();
        message.setParseMode("Markdown");
        message.setText(buildTxtForRewardNotification(airdropList));

        return message;
    }

    /// Build Text for Reward Notification
    private String buildTxtForRewardNotification(List<Airdrop> airdropList) {
        DecimalFormat formatAvg = new DecimalFormat("#,###.##");

        List<String> resultString = new ArrayList<>();
        resultString.add("🎉  *에어드랍 리워드 지급 안내*  🎉\n");

        for (Airdrop each : airdropList) {
            String coin = formatAvg.format(each.getActualReward_coin());
            String krw = formatAvg.format(each.getActualReward_krw());
            resultString.add("*[" + each.getExchange() + "]*");
            resultString.add("🎁 *" + each.getCoin() + " 에어드랍 이벤트*");
            if (!each.getType().equals("Limit")) {
                resultString.add("🍬 리워드 : *" + coin + " " + each.getRewardUnit() +"* (" + krw + " KRW)\n");
            }
        }
        return String.join("\n", resultString);
    }
}
