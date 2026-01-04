package com.nemojin.sosikbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@ToString
@NoArgsConstructor
public class Wallet {
    private String type;
    private String uuid;
    private String currency;
    private String net_type;
    private String txid;
    private String state;
    private OffsetDateTime created_at;
    private OffsetDateTime done_at;
    private double amount;
    private double fee;
    private String transaction_type;
}
