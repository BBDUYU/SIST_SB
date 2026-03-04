package org.doit.ik.complex;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplexRepository extends JpaRepository<Complex, Long> {

	boolean existsByAddress(String address);


	Optional<Complex> findByTitleAndAddressAndType(String title, String address, String type);
	/**
	 * 특정 위도/경도 범위 내의 단지 목록 조회 (필요 시)
	 */
	// List<Complex> findByLatitudeBetweenAndLongitudeBetween(Double latStart, Double latEnd, Double lngStart, Double lngEnd);

	@Query("""
			    SELECT c FROM Complex c
			    WHERE c.latitude BETWEEN :swLat AND :neLat
			    AND c.longitude BETWEEN :swLng AND :neLng
			""")
	List<Complex> findInBounds(double swLat, double swLng, double neLat, double neLng);

	List<Complex> findByLatitudeBetweenAndLongitudeBetween(
			Double swLat, Double neLat,
			Double swLng, Double neLng
			);
	
	// 필터 만들기 위한...
	@Query(value = """
		    SELECT DISTINCT c.*
		    FROM complex c
		    LEFT JOIN room_type r 
		      ON r.cid = c.cid
		     AND r.status = 'ACTIVE'
		    WHERE c.latitude BETWEEN :swLat AND :neLat
		      AND c.longitude BETWEEN :swLng AND :neLng
		      AND (:type IS NULL OR :type = '' OR c.type = :type)
		      AND (:rentType IS NULL OR :rentType = '' OR r.rent_type = :rentType)
		      AND (:areaMin IS NULL OR CAST(r.area AS DECIMAL(10,2)) >= :areaMin)
		      AND (:areaMax IS NULL OR CAST(r.area AS DECIMAL(10,2)) <= :areaMax)
		""", nativeQuery = true)
		List<Complex> findInBoundsWithFilters(
		    @Param("swLat") double swLat,
		    @Param("swLng") double swLng,
		    @Param("neLat") double neLat,
		    @Param("neLng") double neLng,
		    @Param("type") String type,
		    @Param("rentType") String rentType,
		    @Param("areaMin") Double areaMin,
		    @Param("areaMax") Double areaMax
		);
}