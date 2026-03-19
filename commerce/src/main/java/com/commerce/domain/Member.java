package com.commerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;

import java.time.Instant;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String nickname;

    @Column(updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(updatable = true)
    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    private Member(String username, String password, String nickname) {
        Assert.hasText(username, "사용자 아이디는 필수입니다");
        Assert.hasText(password, "비밀번호는 필수입니다");
        Assert.hasText(nickname, "닉네임은 필수입니다");
        Assert.isTrue(username.length() <= 20, "아이디가 너무 깁니다");
        Assert.isTrue(nickname.length() <= 20, "닉네임이 너무 깁니다");

        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }
}
