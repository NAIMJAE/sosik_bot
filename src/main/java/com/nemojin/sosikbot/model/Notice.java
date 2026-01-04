package com.nemojin.sosikbot.model;

import lombok.Getter;
import lombok.ToString;
import com.nemojin.sosikbot.util.DateParser;

import java.time.LocalDate;
@Getter
@ToString
public class Notice {
    private LocalDate date;
    private String link;

    public Notice(String date, String link) {
        this.date = DateParser.stringToLocalDate(date);
        this.link = link;
    }
}
