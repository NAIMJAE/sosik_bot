package com.nemojin.sosikbot.service.event;

import com.nemojin.sosikbot.mapper.AirdropMapper;
import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.Notice;
import com.nemojin.sosikbot.service.crawl.BithumbCrawling;
import com.nemojin.sosikbot.service.crawl.GopaxCrawling;
import com.nemojin.sosikbot.service.interfaces.AirdropCrawl;
import com.nemojin.sosikbot.service.interfaces.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AirdropService implements EventService<Map<String, List<List<Airdrop>>>> {
    private final BithumbCrawling bithumbCrawling;
    private final GopaxCrawling gopaxCrawling;
    private final AirdropMapper airdropMapper;

    /// [Function] Detect New Airdrop Events
    public Map<String, List<List<Airdrop>>> detectNewEvent() throws Exception {
        Map<String, List<List<Airdrop>>> resultMap = new HashMap<>();

        List<List<Airdrop>> bithumbEvents = detectExchangeNewAirdropEvent("Bithumb", bithumbCrawling);
        putIfNotEmpty(resultMap, "Bithumb", bithumbEvents);

        // List<List<Airdrop>> gopaxEvents = detectExchangeNewAirdropEvent("Gopax", gopaxCrawling);
        // putIfNotEmpty(resultMap, "Gopax", gopaxEvents);

        return resultMap;
    }

    /// [Function] Detect New Airdrop Event
    private List<List<Airdrop>> detectExchangeNewAirdropEvent(String exchange, AirdropCrawl exchangeCrawling) throws Exception {
        // step 1: crawl event list page
        List<Notice> noticeList = exchangeCrawling.crawlingEventListPage();
        if (noticeList.isEmpty()) {return null;}

        // step 2: filter new events
        List<Notice> newList = newEventFilter(exchange, noticeList);

        // step 3: crawl event detail pages
        List<List<Airdrop>> airdropList = exchangeCrawling.crawlingAirdropDetail(newList);

        // step 4: insert new Airdrop events into database
        for (List<Airdrop> eventList : airdropList) {
            for (Airdrop each : eventList) {
                airdropMapper.insertNewAirdropEvent(each);
            }
        }
        return airdropList;
    }

    /// [Util] Filter New Airdrop Events by Comparing with Database
    private List<Notice> newEventFilter(String exchange, List<Notice> noticeList) {
        List<Notice> newList = new ArrayList<>();
        for (Notice each : noticeList) {
            List<String> noticeUrl = airdropMapper.selectAirdropForDuplicateCheck(exchange, each.getLink(), each.getDate());

            if (!noticeUrl.contains(each.getLink())){
                newList.add(each);
            }
        }
        return newList;
    }

    /// [Util] Safely Puts Airdrop Events into the Map
    private void putIfNotEmpty(Map<String, List<List<Airdrop>>> map, String exchange, List<List<Airdrop>> events) {
        if (events != null && !events.isEmpty()) {
            map.put(exchange, events);
        }
    }
}
