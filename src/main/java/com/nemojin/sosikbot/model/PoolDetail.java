package com.nemojin.sosikbot.model;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PoolDetail {
    private int poolNo;
    private String launchNo;
    private String name;
    private String total;
    private String minimum;
    private String maximum;

    public PoolDetail(String launchNo, String name, String total, String minimum, String maximum) {
        this.launchNo = launchNo;
        this.name = name;
        this.total = total;
        this.minimum = minimum;
        this.maximum = maximum;
    }
}
