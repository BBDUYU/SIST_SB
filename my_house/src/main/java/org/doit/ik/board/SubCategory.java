package org.doit.ik.board;

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
public class SubCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scId;
    private String scName;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "mc_id")
    private MainCategory mainCategory;
}