package org.doit.ik.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
public class UserTerms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTermsId; // 테이블 설계서의 user_terms 컬럼 대응

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Terms terms;

    @Column(columnDefinition = "CHAR(1)")
    private String agreedYn;
    
    private LocalDateTime agreedAt = LocalDateTime.now();
}