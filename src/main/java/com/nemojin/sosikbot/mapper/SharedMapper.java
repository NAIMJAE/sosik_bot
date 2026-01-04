package com.nemojin.sosikbot.mapper;

import com.nemojin.sosikbot.model.Airdrop;
import com.nemojin.sosikbot.model.LaunchPool;
import com.nemojin.sosikbot.model.Participant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SharedMapper {
    /// [SELECT] Finds today's available airdrop events
    List<Airdrop> selectAirdropsByExchange(@Param("now") LocalDate now);

    /// [SELECT] Finds today's available LaunchPool events
    List<LaunchPool> selectLaunchPoolsByExchange(@Param("now") LocalDate now);

    /// [SELECT] Finds average participants per exchange
    List<Participant> selectAirdropsAvgOfParticipants();

    /// [SELECT] Finds Recent average participants
    List<Participant> selectRecentAvgOfParticipants(@Param("date") LocalDate date);

    /// [SELECT] Finds Month average participants
    List<Participant> selectMonthAvgOfParticipants(@Param("firstDay") String firstDay, @Param("lastDay") String lastDay);

    /// [SELECT] Finds unpaid airdrop events
    List<Airdrop> selectUnpaidAirdrops();

    /// [SELECT] Finds airdrop events for a monthly report
    List<Airdrop> selectAirdropsForMonthlyReport(@Param("firstDay") String firstDay, @Param("lastDay") String lastDay);
}
