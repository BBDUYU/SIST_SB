package org.doit.ik.chat;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class ChatInvite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inviteId;

    @Column(unique = true)
    private String inviteCode; // UUID가 저장될 곳

    private LocalDateTime expiresAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private ChatRoom chatRoom;
    
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.ACTIVE;
}
enum InviteStatus {
    ACTIVE, INACTIVE
}