package com.settlement.batch;

import com.settlement.domain.SettlementRecord;
import com.settlement.domain.SettlementStatus;
import com.settlement.repository.SettlementRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ItemReader;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SettlementRecordItemReader implements ItemReader<SettlementRecord> {

    private final SettlementRecordRepository repository;
    private final Long sellerId;
    private final LocalDate settlementDate;

    private Iterator<SettlementRecord> iterator;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        List<SettlementRecord> records = repository.findBySellerIdAndStatusAndSettlementDate(
                sellerId, SettlementStatus.PENDING, settlementDate);
        this.iterator = records.iterator();
        log.info("정산 대상 로딩 — sellerId={}, date={}, count={}", sellerId, settlementDate, records.size());
    }

    @Override
    public synchronized SettlementRecord read() {
        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
}
