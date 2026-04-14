package com.payment.repository;

import com.payment.domain.Payment;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PaymentRepository extends ReactiveCrudRepository<Payment, Long> {
    Mono<Payment> findByOrderId(@NotNull(message = "주문ID는 필수 값입니다.") Long aLong);
}
