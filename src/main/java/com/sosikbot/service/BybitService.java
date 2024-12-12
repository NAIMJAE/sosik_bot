package com.sosikbot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sosikbot.entity.Airdrop;
import com.sosikbot.entity.LaunchPool;
import com.sosikbot.entity.PoolDetail;
import com.sosikbot.mapper.LaunchPoolMapper;
import com.sosikbot.mapper.PoolDetailMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BybitService {
    
    private final CrawlingService crawlingService;
    private final LaunchPoolMapper launchPoolMapper;
    private final PoolDetailMapper poolDetailMapper;

    //
    public String crawlingLaunchpool() {

        // 최신순 3개 목록 조회해서 파라미터로 넘기기
        List<LaunchPool> launchPoolLast3 = launchPoolMapper.selectLaunchPoolLast3("Bybit");

        // 이름이 일치하면 2중 for문 구동 x
        Map<LaunchPool, List<PoolDetail>> result = crawlingService.bybitLaunchpoolCrawl(launchPoolLast3);

        if (result.size() == 0) {
            return null;
        }

        List<String> resultString = new ArrayList<>();
        resultString.add("💰 Bybit New LaunchPool Event 💰\n");
        
        // DB 저장하기
        for (Map.Entry<LaunchPool, List<PoolDetail>> entry : result.entrySet()) {
            LaunchPool launchPool = entry.getKey();
            List<PoolDetail> poolDetails = entry.getValue();

            LaunchPool launchPoolResult = launchPoolMapper.selectLaunchPool(launchPool);

            if (launchPoolResult == null) {
                launchPoolMapper.insertLaunchPool(launchPool);
            }else {
                continue;
            }

            for (PoolDetail poolDetail : poolDetails) {
                poolDetailMapper.insertLaunchPool(poolDetail);
            }
            resultString.add(launchPoolToString(launchPool, poolDetails));
        }
        return String.join("\n", resultString);
    }

    // SELECT
    public String selectBybitLaunchPool() {

        String dateTimeString = "2024-12-07 14:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);

        List<String> resultString = new ArrayList<>();
        resultString.add("💰 Bybit LaunchPool Event 💰\n");

        List<LaunchPool> launchPoolList = launchPoolMapper.selectLaunchPoolInProgress("Bybit", localDateTime);

        if (launchPoolList.size() > 0) {
            for (LaunchPool launchPool : launchPoolList) {
                List<PoolDetail> poolDetailList = poolDetailMapper.selectPoolDetailList(launchPool.getLaunchNo());
                resultString.add(launchPoolToString(launchPool, poolDetailList));
            }
            return String.join("\n", resultString);
        }else {
            return "💣 현재 진행중인 LaunchPool 이벤트가 없습니다. 💥";
        }
    }

    // LaunchPool Event List ToString
    public String launchPoolToString(LaunchPool launchPool, List<PoolDetail> poolDetailList) {
        List<String> resultString = new ArrayList<>();
        resultString.add("🎁 " + launchPool.getTitle() + " LaunchPool");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");
        resultString.add("🍬 " + launchPool.getStartDate().format(formatter) + " ~ " + launchPool.getEndDate().format(formatter) + " (UTC)\n");

        for (PoolDetail item : poolDetailList) {
            resultString.add("📌 " + item.getName());
            resultString.add("- Total Rewards : " + item.getTotal());
            resultString.add("- Min Staking : " + item.getMinimum());
            resultString.add("- Max Staking : " + item.getMaximum() + "\n");
        }

        return String.join("\n", resultString);
    }
}
