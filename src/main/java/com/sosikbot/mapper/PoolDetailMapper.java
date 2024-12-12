package com.sosikbot.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import com.sosikbot.entity.PoolDetail;

@Mapper
public interface PoolDetailMapper {
    // INSERT New PoolDetail
    @Options(useGeneratedKeys = true, keyProperty = "poolNo")
    void insertLaunchPool(PoolDetail poolDetail);

    // SELECT PoolDetailList of LaunchPool in progress
    List<PoolDetail> selectPoolDetailList(@Param("launchNo") String launchNo);
}
