package org.doit.ik.api;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
public class ApiCollectLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(length = 5, nullable = false)
    private String lawdCd; // 대상 시군구 코드

    @Column(length = 6, nullable = false)
    private String dealYmd; // 수집 대상 월 (예: '202601')

    @Enumerated(EnumType.STRING)
    private CollectStatus status; // SUCCESS, FAIL

    private String errorMessage; // 실패 시 에러 메시지 저장용

    private LocalDateTime collectedAt; // 실행 시간

    @PrePersist
    public void prePersist() {
        this.collectedAt = LocalDateTime.now();
    }
}

enum CollectStatus {
    SUCCESS, FAIL
}