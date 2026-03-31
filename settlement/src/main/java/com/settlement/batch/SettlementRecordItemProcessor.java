package com.settlement.batch;

import com.settlement.domain.SettlementRecord;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class SettlementRecordItemProcessor implements ItemProcessor<SettlementRecord, SettlementRecord> {

    @Override
    public SettlementRecord process(SettlementRecord record) {
        record.settle();
        return record;
    }
}
