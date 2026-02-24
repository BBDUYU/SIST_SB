package org.doit.ik.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Terms {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long termsId;
    
    private String termsCode;
    private String termsName;
    
    @Column(columnDefinition = "CHAR(1)")
    private String isRequired; // 'Y' or 'N'
}