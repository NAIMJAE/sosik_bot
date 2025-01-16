package com.sosikbot.mapper;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import com.sosikbot.entity.Airdrop;

@Mapper
public interface AirdropMapper {
    // INSERT New Airdrop Event Info
    @Options(useGeneratedKeys = true, keyProperty = "no")
    void insertAirdrop(Airdrop airdrop);

    // SELECT Airdrop Event By Exchange
    List<Airdrop> selectAirdropsByExchange(@Param("exchange") String exchange, @Param("today") LocalDate today);

    // Airdrop Event Duplicate Check
    List<Airdrop> airdropsDuplicateCheck(@Param("exchange") String exchange, @Param("title") String title, @Param("startDate") LocalDate startDate);

    // SELECT for Specific Airdrop
    Airdrop selectAirdropByTitle(@Param("exchange") String exchange, @Param("title") String title);

    // SELECT Airdrop Evient Average Number Of Participants
    Integer selectAirdropsAvgOfParticipants(@Param("exchange") String exchange);

    // SELECT Airdrop Evient With Unpaid Reward List
    List<Airdrop> selectAirdropWithUnpaidReward(@Param("exchange") String exchange);
    
    // SELECT Airdrop For Monthly Report
    List<Airdrop> selectAirdropForMonthlyReport(@Param("exchange") String exchange, @Param("firstDay") String firstDay, @Param("lastDay") String lastDay);

    // SELECT Airdrop For Register Reward
    Airdrop selectAirdropByRegister(@Param("exchange") String exchange, @Param("coinName") String coinName, @Param("localDate") LocalDate localDate);

    // INSERT Reward Of Airdrop
    void updateRewardOfAirdrop(Airdrop airdrop);

    // UPDATE Airdrop Event
    void updateAirdrop(Airdrop airdrop);

    // DELETE Airdrop Event
    void deleteAirdrop(int no);
}
