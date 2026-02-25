package org.doit.ik.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"provider", "providerId"})
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 ID로 사용

    @Column(nullable = false)
    private String phone; // 아이디 찾기용

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = true)
    private String password; // 소셜 로그인 대비 NULL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider; // LOCAL, KAKAO, NAVER, GOOGLE

    @Column(nullable = true)
    private String providerId; // 소셜 고유 ID

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
}

