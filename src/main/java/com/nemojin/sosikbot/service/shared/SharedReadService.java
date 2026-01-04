package com.nemojin.sosikbot.service.shared;

import com.nemojin.sosikbot.mapper.SharedMapper;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.LaunchPool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SharedReadService {
    private final SharedMapper sharedMapper;

    // [Function] Get All Available Airdrop List
    public Map<String, List<Airdrop>> getAllAvailableAirdrops() {
        List<Airdrop> airdropList = sharedMapper.selectAirdropsByExchange(LocalDate.now());
        if (airdropList.isEmpty()) {return null;}

        Map<String, List<Airdrop>> result = new HashMap<>();

        for (Airdrop each : airdropList) {
            if (!result.containsKey(each.getExchange())) {
                result.put(each.getExchange(), new ArrayList<>());
            }
            result.get(each.getExchange()).add(each);
        }

        return result;
    }

    // [Function] Get All Available LaunchPool List
    public Map<String, List<LaunchPool>> getAllAvailableLaunchPools() {
        List<LaunchPool> launchPoolList = sharedMapper.selectLaunchPoolsByExchange(LocalDate.now());
        if (launchPoolList.isEmpty()) {return null;}

        Map<String, List<LaunchPool>> result = new HashMap<>();

        for (LaunchPool each : launchPoolList) {
            if (!result.containsKey(each.getExchange())) {
                result.put(each.getExchange(), new ArrayList<>());
            }
            result.get(each.getExchange()).add(each);
        }

        return result;
    }
}
