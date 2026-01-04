package com.nemojin.sosikbot.service.shared;

import com.nemojin.sosikbot.mapper.AirdropMapper;
import com.nemojin.sosikbot.mapper.ChatUserMapper;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.ChatUser;
import com.nemojin.sosikbot.model.Wallet;
import com.nemojin.sosikbot.service.apiService.TickerService;
import com.nemojin.sosikbot.service.apiService.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SharedWriteService {
    private final ChatUserMapper chatUserMapper;
    private final AirdropMapper airdropMapper;
    private final WalletService walletService;
    private final TickerService tickerService;

    // [Function] Registers New User
    public String registerNewUser(String chatId) {
        ChatUser chatUser = chatUserMapper.selectChatUser(chatId);

        if (chatUser == null) {
            ChatUser newChatUser = new ChatUser(chatId, LocalDate.now());
            chatUserMapper.insertChatUser(newChatUser);
            return "🖐️ 안녕하세요 소식봇입니다. 🖐️";
        }else {
            return null;
        }
    }

    // [Function] Registers Airdrop Rewards by Checking Wallet Deposit History
    public List<Airdrop> registerAirdropReward() throws Exception {
        // check today's reward events
        LocalDate now = LocalDate.now();
        List<Airdrop> airdropList = airdropMapper.selectAirdropByPaymentDate(now);
        if (airdropList == null || airdropList.isEmpty()) {return null;}

        // check deposit history from wallet
        List<Airdrop> updateList = new ArrayList<>();
        for (Airdrop airdrop : airdropList) {
            String coinName = airdrop.getRewardUnit();

            switch (airdrop.getExchange()) {
                case "Bithumb" :
                    Wallet wallet = walletService.getBithumbWallet(coinName);

                    if (coinName.equals(wallet.getCurrency()) &&
                            wallet.getState().equals("DEPOSIT_ACCEPTED") &&
                            wallet.getDone_at().toLocalDate().equals(now)) {

                        // update received reward records
                        Map<String, Object> tickerMap = tickerService.getBithumbTickers(coinName);
                        double rewardCoin = wallet.getAmount();
                        Object tradePriceObj = tickerMap.get("trade_price");
                        double tradePrice = ((Number) tradePriceObj).doubleValue();
                        int rewardKrw = (int) (rewardCoin * tradePrice);

                        airdrop.updateActualReward(rewardCoin, rewardKrw);
                        updateList.add(airdrop);
                    }
            }
        }
        if (updateList.isEmpty()) {return null;}

        // update deposit records in DB
        for (Airdrop each : updateList) {
            airdropMapper.updateActualRewardById(each.getNo(), each.getActualReward_coin(), each.getActualReward_krw());
        }

        return updateList;
    }
}
