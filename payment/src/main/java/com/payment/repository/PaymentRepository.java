package com.payment.repository;

import com.payment.domain.Payment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PaymentRepository extends ReactiveCrudRepository<Payment, Long> {
}
