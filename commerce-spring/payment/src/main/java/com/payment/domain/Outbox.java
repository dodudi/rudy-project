package com.payment.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

import java.time.Instant;

@Getter
@Table("outbox")
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

    @Builder
    private Outbox(String topic, String payload) {
        Assert.hasText(topic, "outbox 토픽은 필수 값입니다.");
        Assert.hasText(payload, "outbox 페이로드는 필수 값입니다.");

        this.topic = topic;
        this.payload = payload;
        this.status = OutboxStatus.PENDING.name();
        this.createdAt = Instant.now();
    }

    public Outbox markPublished() {
        this.publishedAt = Instant.now();
        this.status = OutboxStatus.PUBLISHED.name();
        return this;
    }

    public Outbox markFailed() {
        this.publishedAt = null;
        this.status = OutboxStatus.FAILED.name();
        return this;
    }

    public enum OutboxStatus {
        PENDING, PUBLISHED, FAILED
    }
}
