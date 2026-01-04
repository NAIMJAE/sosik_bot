package com.nemojin.sosikbot.bot.command;

import com.nemojin.sosikbot.bot.message.MessageDispatcher;
import com.nemojin.sosikbot.bot.message.MessageService;
import com.nemojin.sosikbot.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@RequiredArgsConstructor
@Service
public class CommandService {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final MessageDispatcher messageDispatcher;
    private final MessageService messageService;

    /// [COMMAND::/alarm] Register User
    public void handleAlarmCommand(String chatId) {
        try {
            SendMessage message = messageService.buildUserRegisterMessage(chatId);
            if (message == null) {return;}

            message.setChatId(chatId);
            messageDispatcher.sendMessage(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("SOSIKBOT COMMAND - End");
        }
    }

    /// [COMMAND::/airdrop] Send Currently Available Airdrop Message
    public void handleAirdropCommand(String chatId) {
        try {
            // build currently available airdrop message
            SendMessage message = messageService.buildAvailableAirdropMessage();
            if (message == null) {
                message = new SendMessage();
                message.setText("💣 현재 진행중인 Airdrop 이벤트가 없습니다. 💥");
            }

            // send message
            message.setChatId(chatId);
            messageDispatcher.sendMessage(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("SOSIKBOT COMMAND - End");
        }
    }

    /// [COMMAND::/launchpool] Send Currently Available LaunchPool Message
    public void handleLaunchPoolCommand(String chatId) {
        try {
            // build today's available launchpool message
            SendMessage message = messageService.buildAvailableLaunchPoolMessage();
            if (message == null) {
                message = new SendMessage();
                message.setText("💣 현재 진행중인 LaunchPool 이벤트가 없습니다. 💥");
            }

            // send message
            message.setChatId(chatId);
            messageDispatcher.sendMessage(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("SOSIKBOT COMMAND - End");
        }
    }

    /// [COMMAND::/reward_date] Send Estimated Reward Date Summary
    public void handleRewardDateCommand(String chatId) {
        try {
            // build message to calculate estimated rewards
            SendMessage message = messageService.buildRewardDateMessage();
            if (message == null) {
                message = new SendMessage();
                message.setText("💣 현재 보상 지급 예정 이벤트가 없습니다. 💥");
            }

            // send message
            message.setChatId(chatId);
            messageDispatcher.sendMessage(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("SOSIKBOT COMMAND - End");
        }
    }

    /// [COMMAND::/recent_average] Send Recent Average Participants per Exchange
    public void handleRecentAverageCommand(String chatId) {
        try {
            // build message to calculate Recent Average Participants
            SendMessage message = messageService.buildRecentAverageMessage();
            if (message == null) {return;}

            // send message
            message.setChatId(chatId);
            messageDispatcher.sendMessage(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("SOSIKBOT COMMAND - End");
        }
    }

    /// [COMMAND::/month_average] Send Month Average Participants per Exchange
    public void handleMonthAverageCommand(String chatId) {
        try {
            // build message to calculate Month Average Participants
            SendMessage message = messageService.buildMonthAverageMessage();
            if (message == null) {return;}

            // send message
            message.setChatId(chatId);
            messageDispatcher.sendMessage(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("SOSIKBOT COMMAND - End");
        }
    }

    /// [COMMAND::/version] Send Now Bot Version
    public void handleVersionCommand(String chatId) {
        try {
            // build message to calculate Recent Average Participants
            SendMessage message = messageService.buildVersionMessage();
            if (message == null) {return;}

            // send message
            message.setChatId(chatId);
            messageDispatcher.sendMessage(message);

        } catch (BusinessException bex) {
            messageDispatcher.sendKnownException(bex);

        } catch (Exception ex) {
            messageDispatcher.sendUnknownException(ex);

        } finally {
            logger.info("SOSIKBOT COMMAND - End");
        }
    }
}
