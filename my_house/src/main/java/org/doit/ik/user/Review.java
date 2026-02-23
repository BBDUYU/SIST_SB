package org.doit.ik.user;

import java.time.LocalDateTime;

import org.doit.ik.complex.Complex;

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
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    private Integer rating;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updateedAt;
    private LocalDateTime deletedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uid")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cid")
    private Complex complex;
    
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.ACTIVE;

    
}
enum ReviewStatus {
    ACTIVE, INACTIVE
}