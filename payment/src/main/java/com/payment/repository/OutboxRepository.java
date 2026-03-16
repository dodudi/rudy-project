package com.payment.repository;

import com.payment.domain.Outbox;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OutboxRepository extends ReactiveCrudRepository<Outbox, Long> {

    Flux<Outbox> findByStatus(String status);
}
