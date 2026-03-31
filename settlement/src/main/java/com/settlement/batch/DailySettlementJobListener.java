package com.settlement.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.settlement.domain.DailySettlement;
import com.settlement.repository.DailySettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DailySettlementJobListener {

    private static final String TOPIC = "settlement.completed";

    private final DailySettlementRepository dailySettlementRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        String settlementDateStr = jobExecution.getJobParameters().getString("settlementDate");
        String jobId = jobExecution.getJobParameters().getString("jobId");

        if (settlementDateStr == null) {
            return;
        }

        LocalDate settlementDate = LocalDate.parse(settlementDateStr);
        List<DailySettlement> dailySettlements = dailySettlementRepository.findBySettlementDate(settlementDate);

        int totalOrderCount = dailySettlements.stream().mapToInt(DailySettlement::getOrderCount).sum();
        long totalAmount = dailySettlements.stream().mapToLong(DailySettlement::getTotalAmount).sum();

        SettlementCompletedEvent event = new SettlementCompletedEvent(
                settlementDate, totalOrderCount, totalAmount, jobId, Instant.now());

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, payload);
            log.info("settlement.completed 발행 — date={}, totalOrderCount={}, totalAmount={}",
                    settlementDate, totalOrderCount, totalAmount);
        } catch (Exception e) {
            log.error("settlement.completed 발행 실패 — date={}", settlementDate, e);
        }
    }
}
