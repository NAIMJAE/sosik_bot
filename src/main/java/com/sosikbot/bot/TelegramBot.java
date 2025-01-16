package com.sosikbot.bot;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.sosikbot.bot.handlers.BotHandler;
import com.sosikbot.bot.handlers.BotImgHandler;
import com.sosikbot.entity.Airdrop;
import com.sosikbot.entity.ChatUser;
import com.sosikbot.service.ChatUserService;
import com.sosikbot.service.Bybit.BybitService;
import com.sosikbot.service.bithumb.BithumbService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class TelegramBot extends TelegramLongPollingBot {
    
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final BotHandler botHandler;
    private final BotImgHandler botImgHandler;
    private final BotUtil botUtil;
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
        
        if (messageText.contains("bithumb_report")) {

            List<String> resultData = botImgHandler.commandHadle(chatId, messageText);
            if (resultData != null) {
                String imgName = resultData.remove(resultData.size() - 1);
                String sendMsg = String.join("\n", resultData);

                String currentDir = Paths.get("").toAbsolutePath().toString();
                Path imagePath = Paths.get(currentDir, "report", imgName);
                File imageFile = imagePath.toFile();

                SendPhoto message = new SendPhoto();
                message.setParseMode("Markdown");
                message.setChatId(chatId);
                message.setPhoto(new InputFile(imageFile));
                message.setCaption(sendMsg);

                try {
                    execute(message);

                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        }else {
            String sendMsg = botHandler.commandHadle(chatId, messageText);
            if (sendMsg != null) {
                SendMessage message = new SendMessage();
                message.setParseMode("Markdown");
                message.setChatId(chatId);
                message.setText(sendMsg);

                try {
                    execute(message);

                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Detect New Events through Scheduling
    @Scheduled(cron = "0 0 11-22 * * *")
    public void sendNewAirdropEventMessage() {
        logger.info(LocalDateTime.now() + " Airdrop Event Scheduled - Start");

        List<List<Airdrop>> crawlingList = bithumbService.crawlingAirdrop();

        if (crawlingList.size() > 0) {
            for (List<Airdrop> airdropList : crawlingList) {
                String messageText = bithumbService.airdropToString(airdropList);

                logger.info(messageText);

                List<ChatUser> chatUserList = chatUserService.selectChatUserList();
                String currentDir = Paths.get("").toAbsolutePath().toString();

                Path imagePath = Paths.get(currentDir, "img", airdropList.get(0).getTitle() + ".png");
                File imageFile = imagePath.toFile();

                SendPhoto message = new SendPhoto();
                message.setParseMode("Markdown");
                message.setPhoto(new InputFile(imageFile));
                message.setCaption(messageText);

                // 버튼 생성
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> row = new ArrayList<>();

                InlineKeyboardButton noticeButton = new InlineKeyboardButton();
                noticeButton.setText("공지사항");
                noticeButton.setUrl(airdropList.get(0).getNoticeURL());
                row.add(noticeButton);

                for(Airdrop airdrop : airdropList) {
                    InlineKeyboardButton transactionButton = new InlineKeyboardButton();
                    transactionButton.setText(airdrop.getCoin() + " 바로가기");
                    transactionButton.setUrl("https://www.bithumb.com/react/trade/order/" + airdrop.getCoin() + "-KRW");
                    row.add(transactionButton);

                    if (row.size() == 2) {
                        rowsInline.add(row);
                        row = new ArrayList<>();
                    }
                }

                rowsInline.add(row);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                message.setReplyMarkup(inlineKeyboardMarkup);

                for(ChatUser chatUser : chatUserList) {
                    message.setChatId(chatUser.getChatId());
                    try {
                        execute(message);
    
                    } catch (TelegramApiException e) {
                        logger.info(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        botUtil.KillChromeProcess();
        logger.info(LocalDateTime.now() + " Airdrop Event Scheduled - End");
    }

    // Detect New LaunchPool Events through Scheduling
    @Scheduled(cron = "0 0 10,22 * * *")
    public void sendNewLaunchPoolEventMessage() {
        logger.info(LocalDateTime.now() + " LaunchPool Event Scheduled - Start");

        String messageText = bybitService.crawlingLaunchpool();

        logger.info(messageText);

        if (messageText != null) {
            List<ChatUser> chatUserList = chatUserService.selectChatUserList();
    
            for(ChatUser chatUser : chatUserList) {
                SendMessage message = new SendMessage();
                message.setParseMode("Markdown");
                message.setChatId(chatUser.getChatId());
                message.setText(messageText);

                // 버튼 생성
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> row = new ArrayList<>();

                InlineKeyboardButton noticeButton = new InlineKeyboardButton();
                noticeButton.setText("Bybit Launchpool");
                noticeButton.setUrl("https://www.bybit.com/en/trade/spot/launchpool");
                row.add(noticeButton);
                rowsInline.add(row);
                inlineKeyboardMarkup.setKeyboard(rowsInline);
                message.setReplyMarkup(inlineKeyboardMarkup);

                try {
                    execute(message);

                } catch (TelegramApiException e) {
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        botUtil.KillChromeProcess();
        logger.info(LocalDateTime.now() + " LaunchPool Event Scheduled - End");
    }

    // Alarm at 11:45 Every Day (Today Airdrop Event)
    @Scheduled(cron = "0 45 23 * * *")
    public void sendAirdropEventMessage() {
        logger.info(LocalDateTime.now() + " Airdrop Event Alarm - Start");

        List<Airdrop> airdropList = bithumbService.alarmBtAirdrop();
        String messageText = bithumbService.alarmAirdropToString(airdropList);

        logger.info(messageText);

        if (airdropList.size() > 0) {
            List<ChatUser> chatUserList = chatUserService.selectChatUserList();

            SendMessage message = new SendMessage();
            message.disableWebPagePreview(); // URL 미리보기 비활성화
            message.setParseMode("Markdown");
            message.setText(messageText);

            // 버튼 생성
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (Airdrop airdrop : airdropList) {
                InlineKeyboardButton transactionButton = new InlineKeyboardButton();
                transactionButton.setText("💰" + airdrop.getCoin() + " 바로가기");
                transactionButton.setUrl("https://www.bithumb.com/react/trade/order/" + airdrop.getCoin() + "-KRW");
                row.add(transactionButton);

                if (row.size() == 2) {
                    rowsInline.add(row);
                    row = new ArrayList<>();
                }
            }

            rowsInline.add(row);
            inlineKeyboardMarkup.setKeyboard(rowsInline);
            message.setReplyMarkup(inlineKeyboardMarkup);
    
            for(ChatUser chatUser : chatUserList) {
                message.setChatId(chatUser.getChatId());
                try {
                    execute(message);
    
                } catch (TelegramApiException e) {
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        botUtil.KillChromeProcess();
        logger.info(LocalDateTime.now() + " Airdrop Event Alarm - End");
    }

    // Alarm at 00:00 Every Day (Airdrop Event Estimated Reward)
    @Scheduled(cron = "0 0 0 * * *")
    public void sendEstimatedRewardMessage() {
        logger.info(LocalDateTime.now() + " Airdrop Estimated Reward Alarm - Start");

        String messageText = bithumbService.alarmEstimatedReward();
        if (messageText != null) {
            List<ChatUser> chatUserList = chatUserService.selectChatUserList();
    
            for(ChatUser chatUser : chatUserList) {
                SendMessage message = new SendMessage();
                message.setParseMode("Markdown");
                message.setChatId(chatUser.getChatId());
                message.setText(messageText);

                try {
                    execute(message);

                } catch (TelegramApiException e) {
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        logger.info(LocalDateTime.now() + " Airdrop Estimated Reward Alarm - End");
    }

    // Alarm at First Of Month (Airdrop Monthly Report)
    @Scheduled(cron = "0 0 12 1 * ?")
    public void AirdropReportAtFirstOfMonth() {
        LocalDate currentDate = LocalDate.now();
        LocalDate previousDate = currentDate.minusMonths(1);

        int previousYear = previousDate.getYear();
        int previousMonth = previousDate.getMonthValue();

        List<String> resultData = bithumbService.airdropMonthlyReport(previousYear, previousMonth, "Scheduled");
        if (resultData == null) {
            return;
        }
        String imgName = resultData.remove(resultData.size() - 1);
        String messageText = String.join("\n", resultData);

        if (messageText != null) {
            List<ChatUser> chatUserList = chatUserService.selectChatUserList();

            String currentDir = Paths.get("").toAbsolutePath().toString();
            Path imagePath = Paths.get(currentDir, "report", imgName);
            File imageFile = imagePath.toFile();
    
            for(ChatUser chatUser : chatUserList) {

                SendPhoto message = new SendPhoto();
                message.setParseMode("Markdown");
                message.setChatId(chatUser.getChatId());
                message.setPhoto(new InputFile(imageFile));
                message.setCaption(messageText);

                try {
                    execute(message);

                } catch (TelegramApiException e) {
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}