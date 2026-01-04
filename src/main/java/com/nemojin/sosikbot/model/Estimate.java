package com.nemojin.sosikbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class Estimate {
    private Airdrop airdrop;
    private double coin;
    private double krw;
}
