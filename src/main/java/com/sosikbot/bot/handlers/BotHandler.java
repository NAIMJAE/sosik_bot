package com.sosikbot.bot.handlers;

import org.springframework.stereotype.Component;

import com.sosikbot.service.BithumbService;
import com.sosikbot.service.BybitService;
import com.sosikbot.service.ChatUserService;

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

        }else if(messageText.contains(Commands.richjo)) {
            return Message.richjoMsg;

        }else {
            return null;
        }
    }

}
