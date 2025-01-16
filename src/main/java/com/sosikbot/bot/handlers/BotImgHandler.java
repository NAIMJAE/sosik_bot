package com.sosikbot.bot.handlers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sosikbot.service.ChatUserService;
import com.sosikbot.service.Bybit.BybitService;
import com.sosikbot.service.bithumb.BithumbService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BotImgHandler {

    private final BithumbService bithumbService;
    
    public List<String> commandHadle(String chatId, String messageText) {
        
        if(messageText.contains(Commands.bt_report)) {

            String[] stArr = messageText.split(" ");
            if (stArr.length != 2) {
                return null;
            }else {
                String[] dateArr = stArr[1].split("\\.");
                return bithumbService.airdropMonthlyReport(Integer.parseInt(dateArr[0])+2000, Integer.parseInt(dateArr[1]), "command");
            }
        }else {
            return null;
        }
    }
}
