package org.doit.ik.user;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"uid", "terms_id"})
    }
)
public class UserTerms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTermsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String agreedYn; // 'Y'

    @Column(nullable = false)
    private LocalDateTime agreedAt = LocalDateTime.now();
}