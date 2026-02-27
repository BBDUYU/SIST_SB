package org.doit.ik.api;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CctvRepository extends JpaRepository<Cctv, Long> {
    
    @Query("SELECT c FROM Cctv c WHERE c.latitude BETWEEN :minLat AND :maxLat " +
           "AND c.longitude BETWEEN :minLng AND :maxLng")
    List<Cctv> findByLocationRange(
        @Param("minLat") Double minLat, 
        @Param("maxLat") Double maxLat, 
        @Param("minLng") Double minLng, 
        @Param("maxLng") Double maxLng
    );
}