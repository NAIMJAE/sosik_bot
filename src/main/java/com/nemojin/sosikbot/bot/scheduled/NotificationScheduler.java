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
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

@RequiredArgsConstructor
@Component
public class NotificationScheduler {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final MessageDispatcher messageDispatcher;
    private final MessageService messageService;

    /// [Scheduled] Daily Airdrop Reminder at 11:45 AM
    /// - Retrieves all airdrop events that are available to participate in today.
    /// - Builds a formatted message with an inline button for each event.
    /// - Sends the message to all subscribed Telegram users.
    // @Scheduled(fixedDelay = 60000)
    @Scheduled(cron = "0 45 23 * * *")
    public void sendDailyAirdropReminder() {
        logger.info("Daily Airdrop Reminder Scheduled - Start");
        try {
            // build today's available airdrop message
            SendMessage message = messageService.buildAvailableAirdropMessage();
            if (message == null) {return;}

            // send message to all users
            messageDispatcher.sendMessageToAll(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("Daily Airdrop Reminder Scheduled - End");
        }
    }

    /// [Scheduled] Daily Estimated Reward Notification at 00:00
    /// - Calculates estimated reward per airdrop based on recent participant averages and coin price.
    /// - Generates a Telegram message summarizing expected coin and KRW rewards.
    /// - Sends the message to all subscribed Telegram users.
    // @Scheduled(fixedDelay = 60000)
    @Scheduled(cron = "0 0 0 * * *")
    public void sendDailyEstimatedReward() {
        logger.info("Daily Estimated Reward Scheduled - Start");
        try {
            // Build message to calculate estimated rewards
            SendMessage message = messageService.buildRewardDateMessage();
            if (message == null) {return;}

            // send message to all users
            messageDispatcher.sendMessageToAll(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("Daily Estimated Reward Scheduled - End");
        }
    }

    /// [Scheduled] Monthly Airdrop Report on the 1st Day of Each Month at 12:00 PM
    /// - Generates a monthly report image summarizing airdrop activity from the previous month.
    /// - Includes statistics such as total events and participation counts.
    /// - Sends the report as an image with a caption to all subscribed Telegram users.
    // @Scheduled(fixedDelay = 60000)
    @Scheduled(cron = "0 0 12 1 * ?")
    public void sendMonthlyAirdropReport() {
        logger.info("Monthly Airdrop Report Scheduled - Start");
        try {
            // Build message for monthly report
            SendPhoto message = messageService.buildMonthlyReportMessage();
            if(message == null) {return;}

            // send message to all users
            messageDispatcher.sendMessageToAll(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("Monthly Airdrop Report Scheduled - End");
        }
    }

    /// [Scheduled] Airdrop Reward Notification
    /// - Checks the wallet's deposit history to verify whether today's scheduled rewards have been successfully deposited.
    /// - Calculates its KRW value based on the current market price and builds the notification message.
    /// - Sends the message to all subscribed Telegram users.
    // @Scheduled(fixedDelay = 60000)
    @Scheduled(cron = "0 0/15 16-20 * * *")
    public void sendRewardNotification() {
        logger.info("Reward Notification Scheduled - Start");
        try {
            // Build message for monthly report
            SendMessage message = messageService.buildRewardNotificationMessage();
            if(message == null) {return;}

            // send message to all users
            messageDispatcher.sendMessageToAll(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("Reward Notification Scheduled - End");
        }
    }
}
