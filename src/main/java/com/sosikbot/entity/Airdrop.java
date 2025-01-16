package com.sosikbot.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "airdrop")
public class Airdrop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int no;
    private String coin;
    private String exchange;
    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDate paymentDate;
    private int totalReward;
    private String rewardUnit;
    private Double actualReward_coin;
    private Double actualReward_krw;

    private String noticeURL;

    public Airdrop(String coin, String exchange, String title, String content, LocalDate startDate, LocalDate endDate, LocalDate paymentDate, int totalReward, String rewardUnit, String noticeURL) {
        this.coin = coin;
        this.exchange = exchange;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
        this.paymentDate = paymentDate;
        this.totalReward = totalReward;
        this.rewardUnit = rewardUnit;
        this.noticeURL = noticeURL;
        this.actualReward_coin = null;
        this.actualReward_krw = null;
    }

    public void setActualRward(double actual_coin, double actual_krw) {
        this.actualReward_coin = actual_coin;
        this.actualReward_krw = actual_krw;
    }
}
