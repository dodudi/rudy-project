package com.settlement.controller;

import com.settlement.dto.DailySettlementResponse;
import com.settlement.dto.SettlementRecordResponse;
import com.settlement.dto.SettlementTriggerRequest;
import com.settlement.service.SettlementJobService;
import com.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settlements")
public class SettlementController {

    private final SettlementService settlementService;
    private final SettlementJobService settlementJobService;

    @GetMapping
    public ResponseEntity<List<SettlementRecordResponse>> getRecords(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(settlementService.getRecords(sellerId, status, startDate, endDate));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailySettlementResponse>> getDailySettlements(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate settlementDate) {
        return ResponseEntity.ok(settlementService.getDailySettlements(sellerId, settlementDate));
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> trigger(
            @RequestBody(required = false) SettlementTriggerRequest request) {
        LocalDate targetDate = (request != null && request.settlementDate() != null)
                ? request.settlementDate()
                : LocalDate.now().minusDays(1);

        String jobId = settlementJobService.triggerJob(targetDate);
        return ResponseEntity.ok(Map.of("jobId", jobId, "status", "STARTED"));
    }
}
