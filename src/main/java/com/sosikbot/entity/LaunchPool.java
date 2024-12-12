package com.sosikbot.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "launchpool")
public class LaunchPool {
    @Id
    private String launchNo; // 14
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
