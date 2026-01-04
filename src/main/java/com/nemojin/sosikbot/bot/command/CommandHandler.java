package com.nemojin.sosikbot.bot.command;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final CommandService commandService;

    /// [Function] Routes User Commands to Service Methods
    public void dispatch(String chatId, String input) {
        Commands command = Commands.from(input);
        if (command == null) {return;}

        logger.info("SOSIKBOT COMMAND - START : " + command.getValue());
        switch (command) {
            case ALARM :
                commandService.handleAlarmCommand(chatId);
                break;

            case AIRDROP :
                commandService.handleAirdropCommand(chatId);
                break;

            case LAUNCHPOOL :
                commandService.handleLaunchPoolCommand(chatId);
                break;

            case REWARD_DATE :
                commandService.handleRewardDateCommand(chatId);
                break;

            case RECENT_AVERAGE :
                commandService.handleRecentAverageCommand(chatId);
                break;

            case MONTH_AVERAGE :
                commandService.handleMonthAverageCommand(chatId);
                break;

            case VERSION :
                commandService.handleVersionCommand(chatId);
                break;


        }
    }
}
