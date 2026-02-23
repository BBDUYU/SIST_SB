package org.doit.ik.user;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    private String name;
    private String email;
    private String phone;
    private String nickname;
    private String loginId;
    private String password;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
}
enum UserStatus {
    ACTIVE, DELETED
}