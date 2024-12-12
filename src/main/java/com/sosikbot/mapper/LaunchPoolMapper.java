package com.sosikbot.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sosikbot.entity.LaunchPool;


@Mapper
public interface LaunchPoolMapper {
    // INSERT New LaunchPool
    void insertLaunchPool(LaunchPool launchpool);

    // SELECT Existing LaunchPool
    LaunchPool selectLaunchPool(LaunchPool launchpool);

    // SELECT LaunchPool in progress
    List<LaunchPool> selectLaunchPoolInProgress(@Param("exchange") String exchange, @Param("today") LocalDateTime today);

    // SELECT LaunchPool Last 3
    List<LaunchPool> selectLaunchPoolLast3(@Param("exchange") String exchange);

}
