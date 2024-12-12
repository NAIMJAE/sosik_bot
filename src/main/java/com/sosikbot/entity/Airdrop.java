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
    private String exchange;
    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;

    public Airdrop(String exchange, String title, String content, LocalDate startDate, LocalDate endDate) {
        this.exchange = exchange;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
