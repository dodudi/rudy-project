package com.settlement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.settlement.domain.SettlementStatus;
import com.settlement.dto.SettlementRecordResponse;
import com.settlement.dto.SettlementTriggerRequest;
import com.settlement.exception.GlobalExceptionHandler;
import com.settlement.service.SettlementJobService;
import com.settlement.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Spring Boot 4.x에서 @WebMvcTest 슬라이스가 제거됨 → standaloneSetup으로 동일 효과
@ExtendWith(MockitoExtension.class)
class SettlementControllerTest {

    @Mock
    private SettlementService settlementService;

    @Mock
    private SettlementJobService settlementJobService;

    private MockMvc mockMvc;

    // 요청 본문 직렬화용 (Jackson 2.x compat layer)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SettlementController(settlementService, settlementJobService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── GET /settlements ─────────────────────────────────────────────────────

    @Test
    void getRecords_withNoFilter_returnsAllRecords() throws Exception {
        List<SettlementRecordResponse> records = List.of(
                new SettlementRecordResponse(1L, 101L, 100L, "key1", 10000,
                        SettlementStatus.PENDING, LocalDate.of(2025, 1, 1), null, Instant.now()),
                new SettlementRecordResponse(2L, 102L, 200L, "key2", 20000,
                        SettlementStatus.SETTLED, LocalDate.of(2025, 1, 1), Instant.now(), Instant.now())
        );
        when(settlementService.getRecords(isNull(), isNull(), isNull(), isNull())).thenReturn(records);

        mockMvc.perform(get("/settlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("SETTLED"));
    }

    @Test
    void getRecords_withSellerIdFilter_returnsOnlySellerRecords() throws Exception {
        List<SettlementRecordResponse> records = List.of(
                new SettlementRecordResponse(1L, 101L, 100L, "key1", 10000,
                        SettlementStatus.PENDING, LocalDate.of(2025, 1, 1), null, Instant.now())
        );
        when(settlementService.getRecords(eq(100L), isNull(), isNull(), isNull())).thenReturn(records);

        mockMvc.perform(get("/settlements").param("sellerId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sellerId").value(100));
    }

    @Test
    void getRecords_withStatusFilter_returnsMatchingRecords() throws Exception {
        List<SettlementRecordResponse> records = List.of(
                new SettlementRecordResponse(2L, 102L, 200L, "key2", 20000,
                        SettlementStatus.SETTLED, LocalDate.of(2025, 1, 1), Instant.now(), Instant.now())
        );
        when(settlementService.getRecords(isNull(), eq("SETTLED"), isNull(), isNull())).thenReturn(records);

        mockMvc.perform(get("/settlements").param("status", "SETTLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("SETTLED"));
    }

    @Test
    void getRecords_withDateRangeFilter_returnsRecordsInRange() throws Exception {
        when(settlementService.getRecords(
                isNull(), isNull(),
                eq(LocalDate.of(2025, 1, 1)),
                eq(LocalDate.of(2025, 1, 31))
        )).thenReturn(List.of());

        mockMvc.perform(get("/settlements")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── POST /settlements/trigger ─────────────────────────────────────────────

    @Test
    void triggerJob_withSettlementDate_returnsJobId() throws Exception {
        when(settlementJobService.triggerJob(any())).thenReturn("job-id-abc");

        SettlementTriggerRequest request = new SettlementTriggerRequest(LocalDate.of(2025, 1, 1));

        mockMvc.perform(post("/settlements/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-id-abc"))
                .andExpect(jsonPath("$.status").value("STARTED"));

        verify(settlementJobService).triggerJob(LocalDate.of(2025, 1, 1));
    }

    @Test
    void triggerJob_withoutBody_usesYesterdayAndReturnsJobId() throws Exception {
        when(settlementJobService.triggerJob(any())).thenReturn("job-id-xyz");

        mockMvc.perform(post("/settlements/trigger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-id-xyz"))
                .andExpect(jsonPath("$.status").value("STARTED"));

        verify(settlementJobService).triggerJob(LocalDate.now().minusDays(1));
    }
}
