package org.doit.ik.complex;

import java.time.LocalDateTime;

import org.doit.ik.user.User;

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
public class RecentlyViewed {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cid")
    private Complex complex;

    private LocalDateTime viewedAt = LocalDateTime.now();
}
