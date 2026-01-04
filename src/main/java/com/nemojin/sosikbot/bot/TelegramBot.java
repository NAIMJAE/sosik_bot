package com.nemojin.sosikbot.bot;

import com.nemojin.sosikbot.bot.command.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final CommandHandler commandHandler;

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
        String chatId = String.valueOf(update.getMessage().getChatId());
        String input = update.getMessage().getText();

        commandHandler.dispatch(chatId, input);
    }
}
