// ✅ User.java (수정본)
package org.doit.ik.user;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "user",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"provider", "providerId"})
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(nullable = true)
    private String name;

    // ✅ 이메일 회원가입/로그인 ID
    @Column(nullable = false, unique = true)
    private String email;

    // ✅ 회원가입에서는 입력 안 받음(나중에 등록 가능)
    // ✅ 아이디찾기(휴대폰) 쓰려면 "등록된 사용자만" 가능
    // ✅ unique=true: 등록된 phone 중복 방지 (NULL 여러 개는 MySQL에서 허용)
    @Column(nullable = true, unique = true)
    private String phone;

    @Column(nullable = false)
    private String nickname;

    // ✅ 이메일 회원가입만 저장(소셜은 null)
    @Column(nullable = true)
    private String password;

    // ✅ LOCAL / KAKAO / NAVER / GOOGLE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    // ✅ 소셜 고유 ID (LOCAL은 null)
    @Column(nullable = true)
    private String providerId;

    // ✅ 권한 기본값 세팅 권장(서비스에서 넣어도 됨)
    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ✅ soft delete
    @Column(nullable = true)
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // ✅ 길이를 20으로 명시적으로 지정
    private UserStatus status = UserStatus.ACTIVE;
}