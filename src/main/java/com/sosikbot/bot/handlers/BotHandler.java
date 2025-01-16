package com.sosikbot.bot.handlers;

import org.springframework.stereotype.Component;

import com.sosikbot.service.ChatUserService;
import com.sosikbot.service.Bybit.BybitService;
import com.sosikbot.service.bithumb.BithumbService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BotHandler {

    private final ChatUserService chatUserService;
    private final BithumbService bithumbService;
    private final BybitService bybitService;

    public String commandHadle(String chatId, String messageText) {
        
        // SOSIK_BOT Alarm /alarm
        if (messageText.contains(Commands.alarm)) {
            return chatUserService.insertChatUser(chatId);

        // Bithumb Airdrop Event List /bt_airdrop
        }else if(messageText.contains(Commands.bt_airdrop)) {
            return bithumbService.selectBtAirdrop();

        }else if(messageText.contains(Commands.bb_launchpool)) {
            return bybitService.selectBybitLaunchPool();

        }else if(messageText.contains(Commands.bt_reward_date)) {
            String testText = bithumbService.alarmEstimatedReward();
            return testText;

        }else if(messageText.contains(Commands.bt_reward_register)) {
            String[] stArr = messageText.split(" ");
            if (stArr.length != 2) {return null;}
            
            String[] dataArr = stArr[1].split("_");
            if (dataArr.length != 3) {return null;}

            String coinName = dataArr[0];
            String rewardCoin = dataArr[1];
            String rewardKRW = dataArr[2];

            String testText = bithumbService.registerAirdropReward(coinName, rewardCoin, rewardKRW);
            return testText;

        }else if(messageText.contains(Commands.richjo)) {
            return Message.richjoMsg;

        }else {
            return null;
        }
    }

}
