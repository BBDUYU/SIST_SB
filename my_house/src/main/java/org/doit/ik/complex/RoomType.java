package org.doit.ik.complex;


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
public class RoomType {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tid;
    private String typeName;
    private String rentType;
    private Integer deposit;
    private Integer monthlyRent;
    private String area;
    
    @Enumerated(EnumType.STRING) 
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.ACTIVE;
    
    private String apiOriginId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cid")
    private Complex complex;
}

enum RoomStatus {
    ACTIVE, INACTIVE
}