package org.doit.ik.user;

import java.time.LocalDateTime;

import org.doit.ik.complex.Complex;

import jakarta.persistence.Entity;
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
public class Wishlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "uid")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cid")
    private Complex complex;
    private LocalDateTime createdAt;
}
