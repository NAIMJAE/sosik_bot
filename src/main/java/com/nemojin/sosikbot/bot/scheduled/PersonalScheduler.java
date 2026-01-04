package com.nemojin.sosikbot.bot.scheduled;

import com.nemojin.sosikbot.bot.message.MessageDispatcher;
import com.nemojin.sosikbot.bot.message.MessageService;
import com.nemojin.sosikbot.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@RequiredArgsConstructor
@Component
public class PersonalScheduler {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final MessageDispatcher messageDispatcher;
    private final MessageService messageService;

    /// @Scheduled(fixedDelay = 60000)
    @Scheduled(cron = "0 10,20,30 23 * * *")
    public void autoTradingForAirdropEvent() {
        logger.info("Auto Trading For Airdrop Event - Start");
        try {

            SendMessage message = messageService.buildAutoTradingForAirdropMessage();
            if (message == null) {return;}

            messageDispatcher.sendMessageToAdmin(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("Auto Trading For Airdrop Event - End");
        }
    }
}
