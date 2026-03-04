package org.doit.ik.api;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LhNoticeRepository extends JpaRepository<LhNotice, String> {
    
    @Query("SELECT l FROM LhNotice l WHERE l.latitude BETWEEN :minLat AND :maxLat " +
           "AND l.longitude BETWEEN :minLng AND :maxLng")
    List<LhNotice> findByLocationRange(Double minLat, Double maxLat, Double minLng, Double maxLng);
}
