package com.nemojin.sosikbot.service.interfaces;

import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.Notice;

import java.util.List;

public interface AirdropCrawl {
    /// [Crawl] Crawling Exchange event List page
    public List<Notice> crawlingEventListPage() throws Exception;

    /// [Crawl] Crawling new Exchange airdrop event detail page
    public List<List<Airdrop>> crawlingAirdropDetail(List<Notice> newList) throws Exception;
}
