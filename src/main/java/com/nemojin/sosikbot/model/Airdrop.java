package com.nemojin.sosikbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor
public class Airdrop {
    private int no;
    private String coin;
    private String exchange;
    private LocalDate date;
    private String type;
    private int consecutive;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate paymentDate;
    private int totalReward;
    private String rewardUnit;
    private Double actualReward_coin;
    private Integer actualReward_krw;
    private String title;
    private String content;
    private String noticeURL;

    public void updateTitleAndCoin(String title, String coin) {
        this.title = title;
        this.coin = coin;
    }
    public void updateDateRange(LocalDate date, LocalDate start, LocalDate end) {
        this.date = date;
        this.startDate = start;
        this.endDate = end;
    }
    public void updateContentTypeAndConsecutive(String content, String type, int day) {
        this.content = content;
        this.type = type;
        this.consecutive = day;
    }
    public void updateRewardInfo(String unit, int total) {
        this.rewardUnit = unit;
        this.totalReward = total;
    }
    public void updatePaymentDate(LocalDate date) {
        this.paymentDate = date;
    }
    public void updateExchangeAndUrl(String exchange, String url) {
        this.exchange = exchange;
        this.noticeURL = url;
    }
    public void updateActualReward(double actual_coin, Integer actual_krw) {
        this.actualReward_coin = actual_coin;
        this.actualReward_krw = actual_krw;
    }
    public boolean isComplete() {
        return title != null && coin != null && type != null && startDate != null && endDate != null
                && content != null && totalReward != 0 && rewardUnit != null && paymentDate != null;
    }
    public boolean hasTitle() {
        return title == null;
    }

    public void deleteTitle() {
        title = null;
    }

}
