package com.settlement.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.settlement.domain.SettlementRecord;
import com.settlement.domain.SettlementStatus;
import com.settlement.repository.SettlementRecordRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = "payment.result",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentResultConsumerTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SettlementRecordRepository settlementRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        settlementRecordRepository.deleteAll();
        Mockito.reset(stringRedisTemplate);

        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
    }

    // ── 1. COMPLETED 이벤트 수신 시 SettlementRecord(PENDING) 생성 확인 ──────────

    @Test
    void completedEvent_shouldCreatePendingSettlementRecord() throws Exception {
        PaymentResultEvent event = new PaymentResultEvent(
                1L, "pay_key_1", 10000, 100L, "COMPLETED", Instant.now());

        kafkaTemplate.send("payment.result", objectMapper.writeValueAsString(event));

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<SettlementRecord> opt = settlementRecordRepository.findByOrderId(1L);
                    assertThat(opt).isPresent();
                    assertThat(opt.get().getStatus()).isEqualTo(SettlementStatus.PENDING);
                    assertThat(opt.get().getSellerId()).isEqualTo(100L);
                    assertThat(opt.get().getGrossAmount()).isEqualTo(10000);
                });
    }

    // ── 2. 동일 orderId 중복 수신 시 중복 레코드 미생성 확인 (Redis mock) ──────────

    @Test
    @SuppressWarnings("unchecked")
    void duplicateCompletedEvent_shouldNotCreateDuplicateRecord() throws Exception {
        PaymentResultEvent event = new PaymentResultEvent(
                2L, "pay_key_2", 20000, 200L, "COMPLETED", Instant.now());
        String message = objectMapper.writeValueAsString(event);

        // Step 1: 첫 번째 메시지 처리 완료까지 대기
        kafkaTemplate.send("payment.result", message);
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(settlementRecordRepository.findByOrderId(2L)).isPresent());

        // Step 2: Redis lock이 이미 잡혀 있는 상태로 mock 변경 (중복 수신 시뮬레이션)
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        // 같은 이벤트 재전송
        kafkaTemplate.send("payment.result", message);

        // Step 3: 일정 시간 후에도 레코드 수 변화 없음 확인
        Awaitility.await()
                .pollDelay(500, TimeUnit.MILLISECONDS)
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(settlementRecordRepository.count()).isEqualTo(1L));
    }

    // ── 3. REFUNDED 이벤트 수신 시 기존 레코드 CANCELLED 처리 확인 ────────────────

    @Test
    void refundedEvent_shouldCancelExistingPendingRecord() throws Exception {
        // COMPLETED 이벤트로 원장 생성
        PaymentResultEvent completedEvent = new PaymentResultEvent(
                3L, "pay_key_3", 15000, 300L, "COMPLETED", Instant.now());
        kafkaTemplate.send("payment.result", objectMapper.writeValueAsString(completedEvent));

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(settlementRecordRepository.findByOrderId(3L)).isPresent());

        // REFUNDED 이벤트로 취소
        PaymentResultEvent refundedEvent = new PaymentResultEvent(
                3L, "pay_key_3", 15000, 300L, "REFUNDED", Instant.now());
        kafkaTemplate.send("payment.result", objectMapper.writeValueAsString(refundedEvent));

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Optional<SettlementRecord> opt = settlementRecordRepository.findByOrderId(3L);
                    assertThat(opt).isPresent();
                    assertThat(opt.get().getStatus()).isEqualTo(SettlementStatus.CANCELLED);
                });
    }
}
