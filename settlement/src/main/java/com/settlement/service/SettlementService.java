package com.settlement.service;

import com.settlement.domain.SettlementStatus;
import com.settlement.dto.DailySettlementResponse;
import com.settlement.dto.SettlementRecordResponse;
import com.settlement.repository.DailySettlementRepository;
import com.settlement.repository.SettlementRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRecordRepository settlementRecordRepository;
    private final DailySettlementRepository dailySettlementRepository;

    @Transactional(readOnly = true)
    public List<SettlementRecordResponse> getRecords(Long sellerId, String status, LocalDate startDate, LocalDate endDate) {
        SettlementStatus settlementStatus = (status != null && !status.isBlank())
                ? SettlementStatus.valueOf(status)
                : null;

        return settlementRecordRepository
                .findWithFilters(sellerId, settlementStatus, startDate, endDate)
                .stream()
                .map(SettlementRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DailySettlementResponse> getDailySettlements(Long sellerId, LocalDate settlementDate) {
        return dailySettlementRepository
                .findWithFilters(sellerId, settlementDate)
                .stream()
                .map(DailySettlementResponse::from)
                .toList();
    }
}
