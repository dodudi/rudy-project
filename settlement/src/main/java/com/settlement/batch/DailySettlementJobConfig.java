package com.settlement.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.settlement.domain.SettlementRecord;
import com.settlement.repository.DailySettlementRepository;
import com.settlement.repository.SettlementRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DailySettlementJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SettlementRecordRepository settlementRecordRepository;
    private final DailySettlementRepository dailySettlementRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Bean
    public Job dailySettlementJob() {
        return new JobBuilder("dailySettlementJob", jobRepository)
                .listener(dailySettlementJobListener())
                .start(masterStep())
                .build();
    }

    @Bean
    public DailySettlementJobListener dailySettlementJobListener() {
        return new DailySettlementJobListener(dailySettlementRepository, kafkaTemplate, objectMapper);
    }

    // ── Master Step (파티셔닝) ──────────────────────────────────────────────

    @Bean
    public Step masterStep() {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner("workerStep", partitioner(null))
                .step(workerStep())
                .gridSize(10)
                .build();
    }

    @Bean
    @StepScope
    public DailySettlementPartitioner partitioner(
            @Value("#{jobParameters['settlementDate']}") String settlementDate) {
        return new DailySettlementPartitioner(
                settlementRecordRepository,
                LocalDate.parse(settlementDate));
    }

    // ── Worker Step (Chunk) ────────────────────────────────────────────────

    @Bean
    public Step workerStep() {
        return new ChunkOrientedStepBuilder<SettlementRecord, SettlementRecord>("workerStep", jobRepository, 1000)
                .reader(reader(null, null))
                .processor(processor())
                .writer(writer(null, null))
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public SettlementRecordItemReader reader(
            @Value("#{stepExecutionContext['sellerId']}") Long sellerId,
            @Value("#{stepExecutionContext['settlementDate']}") String settlementDate) {
        return new SettlementRecordItemReader(
                settlementRecordRepository,
                sellerId,
                LocalDate.parse(settlementDate));
    }

    @Bean
    @StepScope
    public SettlementRecordItemProcessor processor() {
        return new SettlementRecordItemProcessor();
    }

    @Bean
    @StepScope
    public DailySettlementItemWriter writer(
            @Value("#{stepExecutionContext['sellerId']}") Long sellerId,
            @Value("#{stepExecutionContext['settlementDate']}") String settlementDate) {
        return new DailySettlementItemWriter(
                dailySettlementRepository,
                settlementRecordRepository,
                transactionManager,
                sellerId,
                LocalDate.parse(settlementDate));
    }
}
