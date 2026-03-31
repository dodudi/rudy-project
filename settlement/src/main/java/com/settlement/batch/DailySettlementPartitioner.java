package com.settlement.batch;

import com.settlement.domain.SettlementStatus;
import com.settlement.repository.SettlementRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class DailySettlementPartitioner implements Partitioner {

    private final SettlementRecordRepository repository;
    private final LocalDate settlementDate;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<Long> sellerIds = repository.findDistinctSellerIdsByStatusAndSettlementDate(
                SettlementStatus.PENDING, settlementDate);

        log.info("파티션 생성 — date={}, sellerCount={}", settlementDate, sellerIds.size());

        Map<String, ExecutionContext> partitions = new HashMap<>();
        for (int i = 0; i < sellerIds.size(); i++) {
            ExecutionContext context = new ExecutionContext();
            context.putLong("sellerId", sellerIds.get(i));
            context.putString("settlementDate", settlementDate.toString());
            partitions.put("partition" + i, context);
        }
        return partitions;
    }
}
