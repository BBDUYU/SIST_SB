package org.doit.ik.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"termsCode"})
    }
)
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long termsId;

    @Column(nullable = false, unique = true)
    private String termsCode; 
    

    @Column(nullable = false)
    private String termsName;

    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String isRequired; 

    @Column(columnDefinition = "TEXT")
    private String content; 
}