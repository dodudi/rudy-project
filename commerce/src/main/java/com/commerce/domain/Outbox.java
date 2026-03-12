package com.commerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@Table(name = "outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(updatable = false)
    @CreatedDate
    private Instant createdAt;

    private Instant publishedAt;

    public static Outbox create(String topic, String payload) {
        Outbox outbox = new Outbox();
        outbox.topic = topic;
        outbox.payload = payload;
        outbox.status = OutboxStatus.PENDING;
        return outbox;
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public enum OutboxStatus {
        PENDING, PUBLISHED
    }
}
