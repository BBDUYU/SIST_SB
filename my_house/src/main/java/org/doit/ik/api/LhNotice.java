package org.doit.ik.api;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "lh_notice")
public class LhNotice {
    @Id
    private String panId;          // PAN_ID

    private String panNm;          // 공고명
    private String aisTpCdNm;      // 공고세부유형
    private String panSs;          // 공고상태 (공고중만 수집)
    private String dtlUrl;         // 상세 URL
    private String clsgDt;         // 마감일자

    @Column(length = 500)
    private String fullAddress;    // 정제된 주소
    
    private Double latitude;       // 위도
    private Double longitude;      // 경도

    private LocalDateTime updatedAt; // 수집 일시
}