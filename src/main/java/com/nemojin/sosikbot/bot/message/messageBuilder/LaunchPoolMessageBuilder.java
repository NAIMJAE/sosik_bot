package com.nemojin.sosikbot.bot.message.messageBuilder;

import com.nemojin.sosikbot.bot.message.interfaces.MessageBuilder;
import com.nemojin.sosikbot.model.LaunchPool;
import com.nemojin.sosikbot.model.PoolDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class LaunchPoolMessageBuilder implements MessageBuilder<SendMessage, Map<LaunchPool, List<PoolDetail>>> {
    /// [Function] Build New LaunchPool Message
    @Override
    public SendMessage buildMessage(String exchange , Map<LaunchPool, List<PoolDetail>> eventMap) {
        SendMessage message = new SendMessage();
        message.setParseMode("Markdown");
        message.setText(buildLaunchPoolText(exchange, eventMap));
        message.setReplyMarkup(buildLaunchPoolButton(exchange));

        return message;
    }

    /// [Function] Build Message Text for New LaunchPool
    private String buildLaunchPoolText(String exchange, Map<LaunchPool, List<PoolDetail>> eventMap) {
        List<String> resultString = new ArrayList<>();
        resultString.add("💰 *" + exchange + " 런치풀 이벤트 알림* 💰\n");

        for (Map.Entry<LaunchPool, List<PoolDetail>> entry : eventMap.entrySet()) {
            LaunchPool launchPool = entry.getKey();
            List<PoolDetail> poolDetails = entry.getValue();

            resultString.add("🎁 *" + launchPool.getTitle() + " LaunchPool*");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");
            resultString.add("🍬 " + launchPool.getStartDate().format(formatter) + " ~ " + launchPool.getEndDate().format(formatter) + " (UTC)\n");

            for (PoolDetail item : poolDetails) {
                resultString.add("📌 *" + item.getName() + "*");
                resultString.add("- Total Rewards : " + item.getTotal());
                resultString.add("- Min Staking : " + item.getMinimum());
                resultString.add("- Max Staking : " + item.getMaximum() + "\n");
            }
        }
        return String.join("\n", resultString);
    }

    /// [Function] Build Inline Keyboard Button for New Bybit LaunchPool
    private InlineKeyboardMarkup buildLaunchPoolButton(String exchange) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton noticeButton = new InlineKeyboardButton();
        noticeButton.setText(exchange + " Launchpool");
        noticeButton.setUrl(parseExchangeUrl(exchange));
        row.add(noticeButton);
        rowsInline.add(row);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        return inlineKeyboardMarkup;
    }

    ///
    private String parseExchangeUrl(String exchange) {
        switch (exchange) {
            case "Bybit":
                return "https://www.bybit.com/en/trade/spot/launchpool";
        }
        return exchange;
    }
}
