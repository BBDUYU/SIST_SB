package org.doit.ik.api;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lawd_code", indexes = {
    @Index(name = "idx_active_region", columnList = "isActive")
})
@Getter @Setter
@NoArgsConstructor
public class LawdCode {

    @Id
    @Column(length = 5)
    private String lawdCd; // 예: '11680' (시군구 코드 5자리)

    @Column(nullable = false, length = 50)
    private String cityNm; // 예: '서울특별시'

    @Column(nullable = false, length = 50)
    private String sigunguNm; // 예: '강남구'

    @Column(nullable = false)
    private boolean isActive = true; // 수집 대상 여부

    private String lastCollectYmd; // 마지막 수집 성공월 (예: '202602')
}
