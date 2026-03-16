package com.payment.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Table("outbox")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {

    @Id
    private Long id;

    private String topic;

    private String payload;

    private String status;

    @CreatedDate
    private Instant createdAt;

    private Instant publishedAt;

    public static Outbox create(String topic, String payload) {
        return new Outbox(null, topic, payload, OutboxStatus.PENDING.name(), null, null);
    }

    public Outbox markPublished() {
        return new Outbox(this.id, this.topic, this.payload, OutboxStatus.PUBLISHED.name(), this.createdAt, Instant.now());
    }

    public Outbox markFailed() {
        return new Outbox(this.id, this.topic, this.payload, OutboxStatus.FAILED.name(), this.createdAt, this.publishedAt);
    }

    public enum OutboxStatus {
        PENDING, PUBLISHED, FAILED
    }
}
