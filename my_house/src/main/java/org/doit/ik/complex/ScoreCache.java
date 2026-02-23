package org.doit.ik.complex;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
public class ScoreCache {
    @Id
    private Long cid; // Complex의 PK를 공유

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cid")
    private Complex complex;

    private Double safetyScore;
    private Double trafficScore;
    private Double lifeScore;
    private Double totalScore;
}