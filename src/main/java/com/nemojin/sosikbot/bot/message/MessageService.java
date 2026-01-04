package com.nemojin.sosikbot.bot.message;

import com.nemojin.sosikbot.bot.message.messageBuilder.SharedMessageBuilder;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.Estimate;
import com.nemojin.sosikbot.model.LaunchPool;
import com.nemojin.sosikbot.service.shared.SharedLogicService;
import com.nemojin.sosikbot.service.shared.SharedReadService;
import com.nemojin.sosikbot.service.shared.SharedWriteService;
import com.nemojin.sosikbot.service.user.PersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MessageService {
    private final SharedMessageBuilder sharedMessageBuilder;
    private final SharedWriteService sharedWriteService;
    private final SharedReadService sharedReadService;
    private final SharedLogicService sharedLogicService;
    private final PersonalService personalService;

    @Value("${app.version}")
    private String appVersion;

    /// [Function] Build message for user registration
    public SendMessage buildUserRegisterMessage(String chatId) {
        String text = sharedWriteService.registerNewUser(chatId);
        if (text == null) {return null;}

        SendMessage message = new SendMessage();
        message.setParseMode("Markdown");
        message.setText(text);

        return message;
    }

    /// [Function] Build currently Available Airdrop Message
    public SendMessage buildAvailableAirdropMessage() {
        // get all available airdrop
        Map<String, List<Airdrop>> airdropMap = sharedReadService.getAllAvailableAirdrops();
        if (airdropMap == null || airdropMap.isEmpty()) {return null;}

        return sharedMessageBuilder.buildAvailableAirdropMessage(airdropMap);
    }

    /// [Function] Build currently Available LaunchPool Message
    public SendMessage buildAvailableLaunchPoolMessage() {
        // get all available launchpool
        Map<String, List<LaunchPool>> launchpoolMap = sharedReadService.getAllAvailableLaunchPools();
        if (launchpoolMap == null || launchpoolMap.isEmpty()) {return null;}

        return sharedMessageBuilder.buildAvailableLaunchPoolMessage(launchpoolMap);
    }

    /// [Function] Build Message to Calculate Estimated Rewards
    public SendMessage buildRewardDateMessage() {
        // calculate estimated rewards
        Map<String, List<Estimate>> EstimatedMap = sharedLogicService.calculateEstimatedAirdropRewards();
        if (EstimatedMap == null || EstimatedMap.isEmpty()) {return null;}

        return sharedMessageBuilder.buildEstimatedRewardsMessage(EstimatedMap);
    }

    /// [Function] Build Message for Monthly Airdrop Report
    public SendPhoto buildMonthlyReportMessage() {
        // get previous month info
        LocalDate currentDate = LocalDate.now();
        LocalDate previousDate = currentDate.minusMonths(1);
        int previousYear = previousDate.getYear();
        int previousMonth = previousDate.getMonthValue();

        // generate report data and image
        String imageName = "airdrop_report_" + previousYear + "." + previousMonth + ".00.png";
        int[] result = sharedLogicService.createAirdropForMonthlyReport(previousYear, previousMonth, imageName);

        return sharedMessageBuilder.buildMonthlyAirdropReportMessage(previousYear, previousMonth, result[0], result[1], imageName);
    }

    /// [Function] Build Message for Reward Notification
    public SendMessage buildRewardNotificationMessage() throws Exception {
        // Fetch reward events and check deposit history
        List<Airdrop> airdropList = sharedWriteService.registerAirdropReward();
        if (airdropList == null || airdropList.isEmpty()) {return null;}

        return sharedMessageBuilder.buildRewardNotificationMessage(airdropList);
    }

    /// [Function] Build Message to Calculate Recent Average Participants
    public SendMessage buildRecentAverageMessage() {
        // calculate recent average participants
        Map<String, LinkedHashMap<String, Integer>> avgMap = sharedLogicService.CalculateRecentAverageParticipants();

        return sharedMessageBuilder.buildRecentAverageMessage(avgMap);
    }

    /// [Function] Build Message to Calculate Month Average Participants
    public SendMessage buildMonthAverageMessage() throws Exception {
        // calculate recent average participants
        Map<String, List<String>> avgMap = sharedLogicService.CalculateMonthAverageParticipants();

        return sharedMessageBuilder.buildMonthAverageMessage(avgMap);
    }

    /// [Function] Build Message for Now Bot Version
    public SendMessage buildVersionMessage() {
        SendMessage message = new SendMessage();
        message.setText("SOSIK_BOT VERSION :: " + appVersion);

        return message;
    }

    /// [Personal] Build Auto Trading For Airdrop Message
    public SendMessage buildAutoTradingForAirdropMessage() throws Exception {
        // get all available airdrop
        List<String> resultList = personalService.autoTradingProcess();
        if (resultList ==null || resultList.isEmpty()) {return null;}

        resultList.add(0, "🎯 Bithumb Airdrop Auto 결과\n");
        SendMessage message = new SendMessage();
        message.setText(String.join("\n", resultList));

        return message;
    }
}
