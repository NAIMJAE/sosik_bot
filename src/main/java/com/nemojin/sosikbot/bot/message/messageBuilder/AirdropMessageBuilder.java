package com.nemojin.sosikbot.bot.message.messageBuilder;

import com.nemojin.sosikbot.bot.message.interfaces.MessageBuilder;
import com.nemojin.sosikbot.model.Airdrop;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AirdropMessageBuilder implements MessageBuilder<SendPhoto, List<Airdrop>> {
    /// [Function] Build New Airdrop Message
    @Override
    public SendPhoto buildMessage(String exchange, List<Airdrop> eventList) {
        SendPhoto message = new SendPhoto();
        message.setParseMode("Markdown");
        message.setCaption(buildAirdropText(exchange, eventList));
        message.setPhoto(new InputFile(buildAirdropImage(eventList)));
        message.setReplyMarkup(buildAirdropButton(exchange, eventList));

        return message;
    }

    /// Build Text for New Airdrop
    private String buildAirdropText(String exchange, List<Airdrop> eventList) {
        List<String> result = new ArrayList<>();

        if (!eventList.isEmpty()) {
            result.add("🔔 *" + exchange + " 새로운 에어드랍 이벤트* 🔔\n");
            for (Airdrop airdrop : eventList) {
                result.add("🎁 *" + airdrop.getTitle() + "*");
                result.add("🍬 기간 : " + airdrop.getStartDate() + " ~ " + airdrop.getEndDate());
                result.add("🍬 " + airdrop.getContent() + "\n");
            }
        }else {
            result.add("💣 현재 진행중인 Airdrop 이벤트가 없습니다. 💥");
        }
        return String.join("\n", result);
    }

    /// Retrieve Image File Path for New Airdrop
    private File buildAirdropImage(List<Airdrop> eventList) {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path imagePath = Paths.get(currentDir, "img", eventList.get(0).getTitle() + "[" + eventList.get(0).getDate() + "]" + ".png");
        return imagePath.toFile();
    }

    /// Build Inline Keyboard Button for New Airdrop
    private InlineKeyboardMarkup buildAirdropButton(String exchange, List<Airdrop> eventList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton noticeButton = new InlineKeyboardButton();
        noticeButton.setText("공지사항");
        noticeButton.setUrl(eventList.get(0).getNoticeURL());
        row.add(noticeButton);

        for(Airdrop airdrop : eventList) {
            InlineKeyboardButton transactionButton = new InlineKeyboardButton();
            transactionButton.setText(airdrop.getCoin() + " 바로가기");
            transactionButton.setUrl(parseExchangeUrl(exchange) + airdrop.getCoin() + "-KRW");
            row.add(transactionButton);

            if (row.size() == 2) {
                rowsInline.add(row);
                row = new ArrayList<>();
            }
        }
        rowsInline.add(row);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        return inlineKeyboardMarkup;
    }

    ///
    private String parseExchangeUrl(String exchange) {
        switch (exchange) {
            case "Bithumb":
                return "https://www.bithumb.com/react/trade/order/";
        }
        return null;
    }
}
