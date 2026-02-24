package org.doit.ik.complex;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
public class Infrastructure {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long infraId;
    private String infraType;
    private String name;
    private Double latitude;
    private Double longitude;
}