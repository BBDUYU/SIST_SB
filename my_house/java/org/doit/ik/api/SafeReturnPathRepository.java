package org.doit.ik.api;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SafeReturnPathRepository extends JpaRepository<SafeReturnPath, Long> {
	@Query(value = "SELECT MIN(6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) " +
	           "* cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) " +
	           "* sin(radians(latitude)))) * 1000 FROM safe_return_path", 
	           nativeQuery = true)
	    Double findShortestDistance(@Param("lat") Double lat, @Param("lng") Double lng);
	
	List<SafeReturnPath> findByBjdName(String bjdName);
}
