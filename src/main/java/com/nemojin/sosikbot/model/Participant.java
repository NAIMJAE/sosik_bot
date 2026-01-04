package com.nemojin.sosikbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor
public class Participant {
    private String exchange;
    private String coin;
    private LocalDate paymentDate;
    private Integer average;
}
