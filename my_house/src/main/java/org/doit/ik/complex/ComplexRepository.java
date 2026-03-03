package org.doit.ik.complex;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}