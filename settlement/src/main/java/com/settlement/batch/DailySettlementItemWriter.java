package com.settlement.batch;

import com.settlement.domain.DailySettlement;
import com.settlement.domain.SettlementRecord;
import com.settlement.repository.DailySettlementRepository;
import com.settlement.repository.SettlementRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
public class DailySettlementItemWriter implements ItemWriter<SettlementRecord> {

    private final DailySettlementRepository dailySettlementRepository;
    private final SettlementRecordRepository settlementRecordRepository;
    private final PlatformTransactionManager transactionManager;
    private final Long sellerId;
    private final LocalDate settlementDate;

    private int totalOrderCount = 0;
    private long totalAmount = 0L;
    private boolean alreadyExists = false;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        alreadyExists = dailySettlementRepository
                .findBySellerIdAndSettlementDate(sellerId, settlementDate)
                .isPresent();

        if (alreadyExists) {
            log.info("이미 정산 완료된 파티션 skip — sellerId={}, date={}", sellerId, settlementDate);
        }
    }

    @Override
    public void write(Chunk<? extends SettlementRecord> chunk) {
        if (alreadyExists) {
            return;
        }
        // Processor에서 settle()이 호출된 detached 엔티티를 명시적으로 저장
        settlementRecordRepository.saveAll(chunk.getItems());
        for (SettlementRecord record : chunk) {
            totalOrderCount++;
            totalAmount += record.getGrossAmount();
        }
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (alreadyExists || totalOrderCount == 0) {
            return stepExecution.getExitStatus();
        }

        // afterStep은 청크 트랜잭션 밖이므로 TransactionTemplate으로 명시적 트랜잭션 생성
        new TransactionTemplate(transactionManager).execute(status -> {
            DailySettlement daily = DailySettlement.builder()
                    .sellerId(sellerId)
                    .settlementDate(settlementDate)
                    .orderCount(totalOrderCount)
                    .totalAmount(totalAmount)
                    .build();
            dailySettlementRepository.save(daily);
            return null;
        });

        log.info("DailySettlement 생성 — sellerId={}, date={}, orderCount={}, totalAmount={}",
                sellerId, settlementDate, totalOrderCount, totalAmount);

        return stepExecution.getExitStatus();
    }
}
