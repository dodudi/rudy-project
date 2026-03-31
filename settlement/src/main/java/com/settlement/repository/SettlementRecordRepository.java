package com.settlement.repository;

import com.settlement.domain.SettlementRecord;
import com.settlement.domain.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementRecordRepository extends JpaRepository<SettlementRecord, Long> {

    Optional<SettlementRecord> findByOrderId(Long orderId);

    List<SettlementRecord> findByStatusAndSettlementDate(SettlementStatus status, LocalDate settlementDate);

    List<SettlementRecord> findBySellerIdAndStatusAndSettlementDate(Long sellerId, SettlementStatus status, LocalDate settlementDate);

    @Query("SELECT DISTINCT s.sellerId FROM SettlementRecord s WHERE s.status = :status AND s.settlementDate = :settlementDate")
    List<Long> findDistinctSellerIdsByStatusAndSettlementDate(
            @Param("status") SettlementStatus status,
            @Param("settlementDate") LocalDate settlementDate
    );

    @Query("SELECT s FROM SettlementRecord s WHERE " +
            "(:sellerId IS NULL OR s.sellerId = :sellerId) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:startDate IS NULL OR s.settlementDate >= :startDate) AND " +
            "(:endDate IS NULL OR s.settlementDate <= :endDate)")
    List<SettlementRecord> findWithFilters(
            @Param("sellerId") Long sellerId,
            @Param("status") SettlementStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
