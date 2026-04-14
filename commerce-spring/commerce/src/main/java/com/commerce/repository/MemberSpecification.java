package com.commerce.repository;

import com.commerce.domain.Member;
import com.commerce.dto.MemberFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MemberSpecification {

    private MemberSpecification() {}

    public static Specification<Member> withFilters(MemberFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.username() != null) {
                predicates.add(cb.equal(root.get("username"), filter.username()));
            }
            if (filter.nickname() != null) {
                predicates.add(cb.equal(root.get("nickname"), filter.nickname()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
