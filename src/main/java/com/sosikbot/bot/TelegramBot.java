package com.sosikbot.bot;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.sosikbot.bot.handlers.BotHandler;
import com.sosikbot.entity.Airdrop;
import com.sosikbot.entity.ChatUser;
import com.sosikbot.service.BithumbService;
import com.sosikbot.service.BybitService;
import com.sosikbot.service.ChatUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class TelegramBot extends TelegramLongPollingBot {
    
    private final BotHandler botHandler;
    private final BithumbService bithumbService;
    private final BybitService bybitService;
    private final ChatUserService chatUserService;

    @Value("${BOT_TOKEN_KEY}")
    private String botToken;

    @Value("${BOT_NAME}")
    private String botName;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            handleCommands(update);
        } catch (TelegramApiException e) {
            log.info("onUpdateReceived()", e);
        }
    }

    // ChatUser Command Handle
    private void handleCommands(Update update) throws TelegramApiException {
        String chatId = String.valueOf(update.getMessage().getChatId());
        String messageText = update.getMessage().getText();
        
        String sendMsg = botHandler.commandHadle(chatId, messageText);

        if (sendMsg != null) {
            execute(SendMessage.builder().chatId(chatId).text(sendMsg).build());
        }
    }

    // Detect New Events through Scheduling
    @Scheduled(fixedRate = 3600000)
    public void sendNewAirdropEventMessage() {
        List<Airdrop> airdropList = bithumbService.crawlingAirdrop();

        String messageText = bithumbService.airdropToString(airdropList);

        if (airdropList.size() > 0) {
            List<ChatUser> chatUserList = chatUserService.selectChatUserList();
    
            for(ChatUser chatUser : chatUserList) {
                SendMessage message = new SendMessage();
                message.setChatId(chatUser.getChatId());
                message.setText(messageText);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Detect New LaunchPool Events through Scheduling
    @Scheduled(cron = "0 0 9,21 * * *") // 아침 9시와 밤 9시
    public void sendNewLaunchPoolEventMessage() {
        String messageText = bybitService.crawlingLaunchpool();

        if (messageText != null) {
            List<ChatUser> chatUserList = chatUserService.selectChatUserList();
    
            for(ChatUser chatUser : chatUserList) {
                SendMessage message = new SendMessage();
                message.setChatId(chatUser.getChatId());
                message.setText(messageText);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Alarm at 11:45 Every Day
    @Scheduled(cron = "0 45 23 * * *")
    public void sendAirdropEventMessage() {
        String messageText = bithumbService.alarmBtAirdrop();

        if (messageText != null) {
            List<ChatUser> chatUserList = chatUserService.selectChatUserList();
    
            for(ChatUser chatUser : chatUserList) {
                SendMessage message = new SendMessage();
                message.setChatId(chatUser.getChatId());
                message.setText(messageText);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}