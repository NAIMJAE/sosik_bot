package com.nemojin.sosikbot.bot.scheduled;

import com.nemojin.sosikbot.bot.message.MessageDispatcher;
import com.nemojin.sosikbot.bot.message.messageBuilder.AirdropMessageBuilder;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.service.event.AirdropService;
import com.nemojin.sosikbot.util.ChromeManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class AirdropScheduler {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final MessageDispatcher messageDispatcher;
    private final AirdropService airdropService;
    private final AirdropMessageBuilder airdropMessageBuilder;

    /// [Scheduled] Detect New Airdrop Event And Send Message
    /// - Scheduled to run every 30 minutes between 11:00 and 22:30
    // @Scheduled(fixedDelay = 60000)
    @Scheduled(cron = "0 0/30 11-22 * * *")
    public void detectNewAirdropEventAndSendMessage() {
        logger.info("Airdrop Event Scheduled - Start");
        try {
            // step 1: detect new airdrop event
            Map<String, List<List<Airdrop>>> eventMap = airdropService.detectNewEvent();
            if (eventMap.isEmpty()) {return;}

            // step 2: builds and sends the message
            for (Map.Entry<String, List<List<Airdrop>>> entry : eventMap.entrySet()) {
                String exchange = entry.getKey();
                List<List<Airdrop>> airdropList = entry.getValue();

                for (List<Airdrop> eventList : airdropList) {
                    SendPhoto message = airdropMessageBuilder.buildMessage(exchange, eventList);
                    messageDispatcher.sendMessageToAll(message);
                }
            }

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            ChromeManager.KillChromeProcess();
            logger.info("Airdrop Event Scheduled - End");
        }
    }
}
