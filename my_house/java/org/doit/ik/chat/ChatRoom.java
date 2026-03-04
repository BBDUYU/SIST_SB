package org.doit.ik.chat;

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
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;
    private String chatTitle;
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private ChatStatus status = ChatStatus.ACTIVE;
}

enum ChatStatus {
    ACTIVE, INACTIVE
}