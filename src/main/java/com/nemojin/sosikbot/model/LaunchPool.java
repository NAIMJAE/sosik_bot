package com.nemojin.sosikbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@ToString
@NoArgsConstructor
public class LaunchPool {
    private String launchNo;
    private String exchange;
    private String title;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public LaunchPool(String exchange, String title, String status, LocalDateTime startDate, LocalDateTime endDate) {
        this.launchNo = generateLaunchNo();
        this.exchange = exchange;
        this.title = title;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private String generateLaunchNo() {
        return "launch" + UUID.randomUUID().toString().substring(0, 8);
    }
}
