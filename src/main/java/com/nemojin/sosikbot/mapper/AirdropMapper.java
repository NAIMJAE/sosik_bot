package com.nemojin.sosikbot.mapper;

import com.nemojin.sosikbot.model.Airdrop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AirdropMapper {
    /// [INSERT] Insert new airdrop event info
    @Options(useGeneratedKeys = true, keyProperty = "no")
    void insertNewAirdropEvent(Airdrop airdrop);

    /// [SELECT] Check airdrop event for duplicates
    List<String> selectAirdropForDuplicateCheck(@Param("exchange") String exchange,
                                              @Param("noticeUrl") String noticeUrl,
                                              @Param("date")LocalDate date);

    /// [SELECT] Finds Today's Reward Events
    List<Airdrop> selectAirdropByPaymentDate(@Param("paymentDate") LocalDate paymentDate);

    /// [UPDATE] Update Actual Reward
    int updateActualRewardById(@Param("no") int no, @Param("coin") double coin, @Param("krw") int krw);
}
