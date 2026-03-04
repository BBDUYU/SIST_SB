package org.doit.ik.board;

import java.time.LocalDateTime;

import org.doit.ik.user.User;

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
public class Board {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uid")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "sc_id")
    private SubCategory subCategory;
    
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private BoardStatus status = BoardStatus.ACTIVE;
}
enum BoardStatus {
    ACTIVE, INACTIVE
}