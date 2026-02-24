package org.doit.ik.user;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
public class AuthVerification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long verificationId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uid")
    private User user;
    private String verifyType;
    private String channel;
    private String targetValue;
    private String code;
    private LocalDateTime expiresAt;
    private String usedYn;
    private LocalDateTime createdAt;
}
