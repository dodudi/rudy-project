package com.commerce.repository;

import com.commerce.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByMemberId(Long aLong);
}
