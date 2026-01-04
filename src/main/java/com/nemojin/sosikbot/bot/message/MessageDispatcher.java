package com.nemojin.sosikbot.bot.message;

import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.model.ChatUser;
import com.nemojin.sosikbot.service.user.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class MessageDispatcher {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final ChatUserService chatUserService;
    private final MessageSender messageSender;

    @Value("${ROOT_ID}")
    private String rootId;

    /// Send Unknown Exception Message To Admin
    public void sendUnknownException(Exception ex) {
        logger.error(ex.getMessage());

        String msgText = createUnknownMessage(ex);

        SendMessage message = new SendMessage();
        message.setChatId(rootId);
        message.setText(msgText);

        try {
            messageSender.execute(message);
        } catch (TelegramApiException e) {
            throw new BusinessException(BotException.SEND_MESSAGE_FAIL);
        }
    }

    /// Create Unknown Exception Message
    private String createUnknownMessage(Exception ex) {
        List<String> result = new ArrayList<>();

        StackTraceElement element = ex.getStackTrace()[0];
        result.add("[Unknown Exception]");
        result.add(ex.getMessage());
        result.add(element.getClassName());
        result.add(element.getMethodName());
        result.add(String.valueOf(element.getLineNumber()));

        return String.join("\n", result);
    }

    /// Send known Exception Message To Admin
    public void sendKnownException(BusinessException bex) {
        logger.error(bex.getExceptionLog());

        String msgText = createKnownMessage(bex);

        SendMessage message = new SendMessage();
        message.setChatId(rootId);
        message.setText(msgText);

        try {
            messageSender.execute(message);
        } catch (TelegramApiException e) {
            throw new BusinessException(BotException.SEND_MESSAGE_FAIL);
        }
    }

    /// Create Known Exception Message
    private String createKnownMessage(BusinessException bex) {
        List<String> result = new ArrayList<>();

        result.add("[Known Exception]");
        result.add(bex.getType());
        result.add(bex.getContent());
        result.add(bex.getOpinion());
        result.add(bex.getLocation());

        return String.join("\n", result);
    }

    /// Send Text message
    public void sendMessage(SendMessage message) {
        try {
            messageSender.execute(message);
        } catch (TelegramApiException e) {
            throw new BusinessException(BotException.SEND_MESSAGE_FAIL);
        }
    }

    /// Send Photo message
    public void sendMessage(SendPhoto message) {
        try {
            messageSender.execute(message);
        } catch (TelegramApiException e) {
            throw new BusinessException(BotException.SEND_MESSAGE_FAIL);
        }
    }

    /// Send Text message to Admin
    public void sendMessageToAdmin(SendMessage message) {
        try {
            message.setChatId(rootId);
            messageSender.execute(message);
        } catch (TelegramApiException e) {
            throw new BusinessException(BotException.SEND_MESSAGE_FAIL);
        }
    }

    /// Send Text Message to All User
    public void sendMessageToAll(SendMessage message) {
        List<ChatUser> userList = chatUserService.getChatUserList();

        for(ChatUser chatUser : userList) {
            message.setChatId(chatUser.getChatId());
            try {
                messageSender.execute(message);
            } catch (TelegramApiException e) {
                throw new BusinessException(BotException.SEND_MESSAGE_FAIL);
            }
        }
    }

    /// Send Photo Message to All User
    public void sendMessageToAll(SendPhoto message) {
        List<ChatUser> userList = chatUserService.getChatUserList();

        for(ChatUser user : userList) {
            message.setChatId(user.getChatId());
            try {
                messageSender.execute(message);
            } catch (TelegramApiException e) {
                throw new BusinessException(BotException.SEND_MESSAGE_FAIL);
            }
        }
    }
}
