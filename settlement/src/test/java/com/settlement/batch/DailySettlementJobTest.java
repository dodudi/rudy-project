package com.settlement.batch;

import com.settlement.domain.DailySettlement;
import com.settlement.domain.SettlementRecord;
import com.settlement.domain.SettlementStatus;
import com.settlement.repository.DailySettlementRepository;
import com.settlement.repository.SettlementRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DailySettlementJobTest {

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private SettlementRecordRepository settlementRecordRepository;

    @Autowired
    private DailySettlementRepository dailySettlementRepository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        settlementRecordRepository.deleteAll();
        dailySettlementRepository.deleteAll();
    }

    private SettlementRecord pendingRecord(long orderId, long sellerId, String paymentKey, int amount, LocalDate date) {
        Instant completedAt = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        return SettlementRecord.builder()
                .orderId(orderId)
                .sellerId(sellerId)
                .paymentKey(paymentKey)
                .grossAmount(amount)
                .paymentCompletedAt(completedAt)
                .build();
    }

    private JobParameters params(String settlementDate) {
        return new JobParametersBuilder()
                .addString("settlementDate", settlementDate)
                .addString("jobId", UUID.randomUUID().toString())
                .toJobParameters();
    }

    // ── 1. PENDING 2건 → DailySettlement 1건 생성, totalAmount 합산 확인 ────────

    @Test
    void run_createsDailySettlementWithSumOfPendingRecords() throws Exception {
        LocalDate date = LocalDate.of(2025, 1, 10);
        settlementRecordRepository.saveAll(List.of(
                pendingRecord(1L, 100L, "key1", 10000, date),
                pendingRecord(2L, 100L, "key2", 20000, date)
        ));

        JobExecution execution = jobOperatorTestUtils.startJob(params(date.toString()));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<DailySettlement> dailySettlements = dailySettlementRepository.findBySettlementDate(date);
        assertThat(dailySettlements).hasSize(1);

        DailySettlement ds = dailySettlements.get(0);
        assertThat(ds.getSellerId()).isEqualTo(100L);
        assertThat(ds.getOrderCount()).isEqualTo(2);
        assertThat(ds.getTotalAmount()).isEqualTo(30000L);
    }

    // ── 2. 실행 후 원장 레코드 상태 SETTLED 확인 ────────────────────────────────

    @Test
    void run_settlesAllPendingRecords() throws Exception {
        LocalDate date = LocalDate.of(2025, 1, 11);
        settlementRecordRepository.saveAll(List.of(
                pendingRecord(3L, 100L, "key3", 5000, date),
                pendingRecord(4L, 100L, "key4", 8000, date)
        ));

        JobExecution execution = jobOperatorTestUtils.startJob(params(date.toString()));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<SettlementRecord> records = settlementRecordRepository.findAll();
        assertThat(records).isNotEmpty()
                .allSatisfy(r -> assertThat(r.getStatus()).isEqualTo(SettlementStatus.SETTLED));
    }

    // ── 3. 동일 settlementDate 재실행 시 중복 집계 없음 확인 ─────────────────────

    @Test
    void rerun_withSameSettlementDate_doesNotDuplicateDailySettlement() throws Exception {
        LocalDate date = LocalDate.of(2025, 1, 12);
        settlementRecordRepository.saveAll(List.of(
                pendingRecord(5L, 100L, "key5", 12000, date),
                pendingRecord(6L, 100L, "key6", 18000, date)
        ));

        // 1차 실행
        JobExecution first = jobOperatorTestUtils.startJob(params(date.toString()));
        assertThat(first.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 2차 실행 (jobId 다름, settlementDate 동일)
        JobExecution second = jobOperatorTestUtils.startJob(params(date.toString()));
        assertThat(second.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // DailySettlement 중복 없음
        List<DailySettlement> dailySettlements = dailySettlementRepository.findBySettlementDate(date);
        assertThat(dailySettlements).hasSize(1);
        assertThat(dailySettlements.getFirst().getTotalAmount()).isEqualTo(30000L);
    }
}
