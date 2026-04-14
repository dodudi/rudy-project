package com.commerce.repository;

import com.commerce.domain.Wallet;
import com.commerce.dto.WalletFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class WalletSpecification {

    private WalletSpecification() {
    }

    public static Specification<Wallet> withFilter(WalletFilterRequest filter) {
        List<Predicate> predicates = new ArrayList<>();

        return (root, query, cb) -> {
            if (filter.memberId() != null) {
                predicates.add(cb.equal(root.get("member").get("id"), filter.memberId()));
            }

            if (filter.hasBalance() != null) {
                predicates.add(filter.hasBalance()
                        ? cb.greaterThan(root.get("balance"), 0)
                        : cb.equal(root.get("balance"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
