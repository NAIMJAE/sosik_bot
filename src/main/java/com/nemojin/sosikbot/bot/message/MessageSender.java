package com.nemojin.sosikbot.bot.message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Component
public class MessageSender extends DefaultAbsSender {
    @Value("${BOT_TOKEN_KEY}")
    private String botToken;

    public MessageSender(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
