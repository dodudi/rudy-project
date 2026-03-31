package com.settlement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementJobService {

    private final JobOperator jobOperator;
    private final Job dailySettlementJob;

    public String triggerJob(LocalDate settlementDate) {
        String jobId = UUID.randomUUID().toString();

        JobParameters params = new JobParametersBuilder()
                .addString("settlementDate", settlementDate.toString())
                .addString("jobId", jobId)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobOperator.start(dailySettlementJob, params);
            log.info("정산 배치 실행 — date={}, jobId={}", settlementDate, jobId);
        } catch (Exception e) {
            log.error("정산 배치 실행 실패 — date={}, jobId={}", settlementDate, jobId, e);
            throw new RuntimeException("정산 배치 실행에 실패했습니다.", e);
        }

        return jobId;
    }
}
