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
@Table(name = "safe_return_path")
@Getter @Setter
public class SafeReturnPath {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String detailLocation;   // 세부위치 (필운대로5길)
    private String sigungu;          // 시군구명 (종로구)
    private String bjdName;          // 읍면동명 (누하동)
    private Integer bellCount;       // 안심벨
    private Integer cctvCount;       // CCTV
    private Integer lampCount;       // 보안등
    
    @Column(columnDefinition = "TEXT")
    private String pathCoordinates; 

    private LocalDateTime updatedAt;
}