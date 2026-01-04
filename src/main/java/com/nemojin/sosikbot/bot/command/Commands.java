package com.nemojin.sosikbot.bot.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Commands {
    /**
     * Commands Guide
     * - bt : Bithumb
     * - ub : UPbit
     * - kb : Korbit
     * - co : Coinone
     * 
     * - bn : Binance
     * - bb : Bybit
     */

    ALARM("/alarm"),
    AIRDROP("/airdrop"),
    LAUNCHPOOL("/launchpool"),
    REWARD_DATE("/reward_date"),
    RECENT_AVERAGE("/recent_average"),
    MONTH_AVERAGE("/month_average"),
    VERSION("/version");

    private final String value;

    public static Commands from(String command) {
        if (command.contains("@")) {
            command = command.split("@")[0];
        }
        for (Commands each : values()) {
            if (each.getValue().equals(command)) {
                return each;
            }
        }
        return null;
    }

}
