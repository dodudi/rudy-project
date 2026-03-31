package com.settlement.repository;

import com.settlement.domain.DailySettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {

    Optional<DailySettlement> findBySellerIdAndSettlementDate(Long sellerId, LocalDate settlementDate);

    List<DailySettlement> findBySettlementDate(LocalDate settlementDate);

    @Query("SELECT d FROM DailySettlement d WHERE " +
            "(:sellerId IS NULL OR d.sellerId = :sellerId) AND " +
            "(:settlementDate IS NULL OR d.settlementDate = :settlementDate)")
    List<DailySettlement> findWithFilters(
            @org.springframework.data.repository.query.Param("sellerId") Long sellerId,
            @org.springframework.data.repository.query.Param("settlementDate") LocalDate settlementDate
    );
}
