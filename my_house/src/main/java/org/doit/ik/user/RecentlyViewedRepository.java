package org.doit.ik.user;

import java.util.List;

import org.doit.ik.complex.RecentlyViewed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, Long> {

    // user.uid 기반 (최근 4개)
    List<RecentlyViewed> findTop4ByUser_UidOrderByViewedAtDesc(Long uid);

    long countByUser_Uid(Long uid);
}