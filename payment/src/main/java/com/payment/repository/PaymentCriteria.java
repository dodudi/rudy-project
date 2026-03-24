package com.payment.repository;

import com.payment.dto.PaymentFilterRequest;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;

public class PaymentCriteria {

    private PaymentCriteria() {}

    public static Query withFilters(PaymentFilterRequest filter) {
        Criteria criteria = Criteria.empty();

        if (filter.memberId() != null) {
            criteria = criteria.and("member_id").is(filter.memberId());
        }
        if (filter.orderId() != null) {
            criteria = criteria.and("order_id").is(filter.orderId());
        }
        if (filter.status() != null && !filter.status().isBlank()) {
            criteria = criteria.and("status").is(filter.status());
        }
        if (filter.startDate() != null) {
            criteria = criteria.and("created_at").greaterThanOrEquals(filter.startDate());
        }
        if (filter.endDate() != null) {
            criteria = criteria.and("created_at").lessThanOrEquals(filter.endDate());
        }
        if (filter.minAmount() != null) {
            criteria = criteria.and("amount").greaterThanOrEquals(filter.minAmount());
        }
        if (filter.maxAmount() != null) {
            criteria = criteria.and("amount").lessThanOrEquals(filter.maxAmount());
        }

        return Query.query(criteria);
    }
}
