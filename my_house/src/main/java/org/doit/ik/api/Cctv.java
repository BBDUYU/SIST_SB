package org.doit.ik.api;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
@Table(name = "cctv")
public class Cctv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String agency;       // 관리기관명
    private String addressRoad;  // 소재지도로명주소
    private String addressJibun; // 소재지지번주소
    @Enumerated(EnumType.STRING) // DB에는 Enum의 이름(VIGILANCE 등)이 문자열로 저장됩니다.
    private CctvPurpose purpose;
    private Integer count;       // 카메라대수
    private Double latitude;     // WGS84위도
    private Double longitude;    // WGS84경도
    private String dataDate;     // 데이터기준일자
}
