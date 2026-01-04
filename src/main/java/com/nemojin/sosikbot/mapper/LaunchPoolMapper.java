package com.nemojin.sosikbot.mapper;

import com.nemojin.sosikbot.model.LaunchPool;
import com.nemojin.sosikbot.model.PoolDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LaunchPoolMapper {
    /// [INSERT] Insert new LaunchPool
    int insertNewLaunchPoolEvent(LaunchPool launchPool);

    /// [INSERT] Insert new poolDetail
    void insertNewLaunchPoolDetail(PoolDetail poolDetail);

    /// [SELECT] Finds LaunchPool Last 3
    List<LaunchPool> selectLaunchPoolLast3(@Param("exchange") String exchange);
}
