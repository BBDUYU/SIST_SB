package org.doit.ik.complex;

import java.time.LocalDateTime;

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
public class Complex {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cid;
    private String title;
    private String address;
    private String fullName;
    private Double latitude;
    private Double longitude;
    private String type;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime createdAt;
    
    public void updateFullName() {
        this.fullName = (this.address != null ? this.address : "") + " " + 
                        (this.title != null ? this.title : "");
    }
}
