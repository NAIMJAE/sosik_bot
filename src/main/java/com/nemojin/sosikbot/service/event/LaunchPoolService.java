package com.nemojin.sosikbot.service.event;

import com.nemojin.sosikbot.mapper.LaunchPoolMapper;
import com.nemojin.sosikbot.model.LaunchPool;
import com.nemojin.sosikbot.model.PoolDetail;
import com.nemojin.sosikbot.service.crawl.BybitCrawling;
import com.nemojin.sosikbot.service.interfaces.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class LaunchPoolService implements EventService<Map<String, Map<LaunchPool, List<PoolDetail>>>> {
    private final BybitCrawling bybitCrawling;
    private final LaunchPoolMapper launchPoolMapper;

    /// [Function] Detect New LaunchPool Events
    public Map<String, Map<LaunchPool, List<PoolDetail>>> detectNewEvent() throws Exception {
        Map<LaunchPool, List<PoolDetail>> bybitEvents = detectBybitNewLaunchPoolEvent();

        Map<String, Map<LaunchPool, List<PoolDetail>>> resultMap = new HashMap<>();
        putIfNotEmpty(resultMap, "Bybit", bybitEvents);

        return resultMap;
    }

    /// [Function] Detect Bybit New LaunchPool Event
    public Map<LaunchPool, List<PoolDetail>> detectBybitNewLaunchPoolEvent() throws Exception {
        // step 1: finds LaunchPool last 3
        List<LaunchPool> lastEvent = launchPoolMapper.selectLaunchPoolLast3("Bybit");

        // step 2: crawl event page
        Map<LaunchPool, List<PoolDetail>> eventMap = bybitCrawling.crawlingLaunchPoolEventPage(lastEvent);

        // step 3: insert new LaunchPool and poolDetail
        for (Map.Entry<LaunchPool, List<PoolDetail>> entry : eventMap.entrySet()) {
            LaunchPool launchPool = entry.getKey();
            List<PoolDetail> poolDetails = entry.getValue();

            int result = launchPoolMapper.insertNewLaunchPoolEvent(launchPool);
            if (result == 0) {continue;}

            for (PoolDetail poolDetail : poolDetails) {
                launchPoolMapper.insertNewLaunchPoolDetail(poolDetail);
            }
        }
        return eventMap;
    }

    /// [Util] Safely Puts LaunchPool Events into the Map
    private void putIfNotEmpty(Map<String, Map<LaunchPool, List<PoolDetail>>> map, String exchange, Map<LaunchPool, List<PoolDetail>> events) {
        if (events != null && !events.isEmpty()) {
            map.put(exchange, events);
        }
    }
}
