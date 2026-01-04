package com.nemojin.sosikbot.bot.scheduled;

import com.nemojin.sosikbot.bot.message.MessageDispatcher;
import com.nemojin.sosikbot.bot.message.messageBuilder.LaunchPoolMessageBuilder;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.model.LaunchPool;
import com.nemojin.sosikbot.model.PoolDetail;
import com.nemojin.sosikbot.service.event.LaunchPoolService;
import com.nemojin.sosikbot.util.ChromeManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class LaunchPoolScheduler {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final MessageDispatcher messageDispatcher;
    private final LaunchPoolService launchPoolService;
    private final LaunchPoolMessageBuilder launchPoolMessageBuilder;

    /// [Scheduled] Detect New LaunchPool Event And Send Message
    /// - Scheduled to run every 10:00 and 22:00
    // @Scheduled(fixedDelay = 60000)
    // @Scheduled(cron = "0 0 10,22 * * *")
    public void detectNewLaunchPoolEventAndSendMessage() {
        logger.info("LaunchPool Event Scheduled - Start");
        try {
            // step 1: detect new launchPool event
            Map<String, Map<LaunchPool, List<PoolDetail>>> eventMap = launchPoolService.detectNewEvent();
            if (eventMap.isEmpty()) {return;}

            // step 2: builds and sends the message
            for (Map.Entry<String, Map<LaunchPool, List<PoolDetail>>> entry : eventMap.entrySet()) {
                String exchange = entry.getKey();
                Map<LaunchPool, List<PoolDetail>> launchPoolMap = entry.getValue();

                SendMessage message = launchPoolMessageBuilder.buildMessage(exchange, launchPoolMap);
                messageDispatcher.sendMessageToAll(message);
            }

        }catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            ChromeManager.KillChromeProcess();
            logger.info("LaunchPool Event Scheduled - End");
        }
    }
}
