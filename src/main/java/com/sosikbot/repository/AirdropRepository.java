package com.sosikbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sosikbot.entity.Airdrop;

public interface AirdropRepository extends JpaRepository<Airdrop, Integer> {
    
}
