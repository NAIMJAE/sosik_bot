package com.sosikbot.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sosikbot.entity.Airdrop;
import com.sosikbot.mapper.AirdropMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BithumbService {

    private final CrawlingService crawlingService;
    private final AirdropMapper airdropMapper;
    
    // Crawling Bithumb Airdrop Event
    public List<Airdrop> crawlingAirdrop() {
        List<Airdrop> airdropList = crawlingService.bithumbAirdropCrawl();

        List<Airdrop> resultAirdropList = new ArrayList<>();
        
        for (Airdrop airdrop : airdropList) {
            if (airdrop.getStartDate() != null && airdrop.getEndDate() != null) {
                List<Airdrop> duplicateAirdrop = airdropMapper.airdropsDuplicateCheck(airdrop.getExchange(), airdrop.getTitle(), airdrop.getStartDate());

                if (duplicateAirdrop.size() < 1) {
                    airdropMapper.insertAirdrop(airdrop);
                    resultAirdropList.add(airdrop);
                }
            }else {
                Airdrop oldAirdrop = airdropMapper.selectAirdropByTitle(airdrop.getExchange(), airdrop.getTitle());

                if (oldAirdrop != null) {
                    airdropMapper.deleteAirdrop(oldAirdrop.getNo());
                }
            }
        }
        return resultAirdropList;
    }

    // SELECT Bithumb Airdrop Event List By Date
    public String selectBtAirdrop() {
        List<Airdrop> airdropList = airdropMapper.selectAirdropsByExchange("bithumb", LocalDate.now());
        return airdropToString(airdropList);
    }

    // SELECT Bithumb Airdrop Event For Alarm
    public String alarmBtAirdrop() {
        List<Airdrop> airdropList = airdropMapper.selectAirdropsByExchange("bithumb", LocalDate.now());
        return alarmAirdropToString(airdropList);
    }

    // Airdrop Event List ToString For New Event Alarm
    public String airdropToString(List<Airdrop> airdropList) {
        List<String> resultString = new ArrayList<>();
        resultString.add("💰 Bithumb New Airdrop Event 💰\n");

        if (!airdropList.isEmpty()) {
            for (Airdrop item : airdropList) {
                if(item.getStartDate() != null && item.getEndDate() != null) {
                    resultString.add("🎁 " + item.getTitle());
                    resultString.add("🍬 기간 : " + item.getStartDate() + " ~ " + item.getEndDate());
                    resultString.add("🍬 " + item.getContent() + "\n");
                }
            }
        }else {
            resultString.add("💣 현재 진행중인 Airdrop 이벤트가 없습니다. 💥");
        }
        return String.join("\n", resultString);
    }

    // Airdrop Event List ToString For Every Day Alarm
    public String alarmAirdropToString(List<Airdrop> airdropList) {
        List<String> resultString = new ArrayList<>();
        resultString.add("🔔 Bithumb Airdrop Event Alarm 🔔\n");

        if (!airdropList.isEmpty()) {
            for (Airdrop item : airdropList) {
                if(item.getStartDate() != null && item.getEndDate() != null) {

                    // 괄호 안의 단어를 추출하는 정규식
                    Pattern pattern = Pattern.compile("\\((.*?)\\)");
                    Matcher matcher = pattern.matcher(item.getTitle());

                    resultString.add("🎁 " + item.getTitle());
                    resultString.add("🍬 기간 : " + item.getStartDate() + " ~ " + item.getEndDate());
                    resultString.add("🍬 " + item.getContent() + "\n");

                    while (matcher.find()) {
                        String wordInParentheses = matcher.group(1);
                        resultString.add("https://www.bithumb.com/react/trade/order/" + wordInParentheses + "-KRW");
                    }
                }
            }
            return String.join("\n", resultString);

        }else {
            return null;
        }
    }
}
