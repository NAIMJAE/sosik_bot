package com.nemojin.sosikbot.service.user;

import com.nemojin.sosikbot.exception.BotException;
import com.nemojin.sosikbot.exception.BusinessException;
import com.nemojin.sosikbot.mapper.SharedMapper;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.service.apiService.TickerService;
import com.nemojin.sosikbot.service.apiService.WalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PersonalService {
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");
    private final TickerService tickerService;
    private final WalletService walletService;
    private final SharedMapper sharedMapper;

    // [Function] Auto Trading Process For Bithumb Airdrop Event
    public List<String> autoTradingProcess() throws Exception{
        // get today available airdrop event list
        List<Airdrop> airdropList = sharedMapper.selectAirdropsByExchange(LocalDate.now());
        if (airdropList.isEmpty()) {return null;}

        List<String> resultMsg = new ArrayList<>();

        for (Airdrop event : airdropList) {
            try {
                // get today transaction history
                boolean prevResult = walletService.getTransactionHistory("KRW-" + event.getCoin());
                if (!prevResult) {continue;}

                // get now price
                Map<String, Object> tickerMap = tickerService.getBithumbTickers(event.getCoin());
                if (tickerMap.isEmpty()) {
                    resultMsg.add(event.getCoin() + " 상장 전");
                    continue;
                }

                // submit bid order
                String tradeUUID = walletService.submitOrder("KRW-" + event.getCoin(), "bid", BigDecimal.valueOf(0), 5100, "price");
                if (tradeUUID == null) {
                    resultMsg.add(event.getCoin() + " 매수 주문 실패");
                    continue;
                }

                // get bid order history
                Thread.sleep(10000);
                BigDecimal tradeVolume = walletService.getTransactionAtUuid(tradeUUID);

                // submit ask order
                String result = walletService.submitOrder("KRW-" + event.getCoin(), "ask", tradeVolume, 0, "market");
                if (result == null) {
                    resultMsg.add(event.getCoin() + " 매도 주문 실패");
                    continue;
                }

                resultMsg.add(event.getCoin() + " 매매 주문 완료");
            }catch (Exception e) {
                throw new BusinessException(BotException.AUTO_TRADING_ERROR);
            }
        }
        return resultMsg;
    }
}
