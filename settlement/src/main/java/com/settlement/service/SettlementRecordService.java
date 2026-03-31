package com.settlement.service;

import com.settlement.domain.SettlementRecord;
import com.settlement.event.PaymentResultEvent;
import com.settlement.repository.SettlementRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementRecordService {

    private static final String LOCK_PREFIX = "settlement:lock:";
    private static final Duration LOCK_TTL = Duration.ofHours(24);

    private final SettlementRecordRepository settlementRecordRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void createRecord(PaymentResultEvent event) {
        String lockKey = LOCK_PREFIX + event.orderId();

        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            log.warn("정산 원장 중복 생성 시도 무시 — orderId={}", event.orderId());
            return;
        }

        if (settlementRecordRepository.findByOrderId(event.orderId()).isPresent()) {
            log.warn("이미 존재하는 정산 원장 — orderId={}", event.orderId());
            return;
        }

        SettlementRecord record = SettlementRecord.builder()
                .orderId(event.orderId())
                .sellerId(event.sellerId())
                .paymentKey(event.paymentKey())
                .grossAmount(event.amount())
                .paymentCompletedAt(event.completedAt())
                .build();

        settlementRecordRepository.save(record);
        log.info("정산 원장 생성 — orderId={}, sellerId={}, amount={}", event.orderId(), event.sellerId(), event.amount());
    }

    @Transactional
    public void cancelRecord(PaymentResultEvent event) {
        settlementRecordRepository.findByOrderId(event.orderId())
                .ifPresentOrElse(
                        record -> {
                            record.cancel();
                            log.info("정산 원장 취소 — orderId={}", event.orderId());
                        },
                        () -> log.warn("취소할 정산 원장 없음 (REFUNDED 선행 수신) — orderId={}", event.orderId())
                );
    }
}
