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
    private String panId;          // PAN_ID (공고 ID를 PK로 사용)

    private String panNm;          // 공고명 (PAN_NM)
    private String aisTpCdNm;      // 공고세부유형 (행복주택 등)
    private String panSs;          // 공고상태 (공고중 등)
    private String dtlUrl;         // 상세 URL (DTL_URL)
    private String clsgDt;         // 마감일자 (CLSG_DT)

    // 상세 정보(dsSbd)에서 가져올 주소
    private String fullAddress;    // LCT_ARA_ADR + LCT_ARA_DTL_ADR 합친 주소
    
    // 매칭을 위한 좌표
    private Double latitude;
    private Double longitude;

    private LocalDateTime updatedAt; // 수집 일시
}