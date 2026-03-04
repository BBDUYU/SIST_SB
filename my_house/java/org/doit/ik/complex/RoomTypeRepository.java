package org.doit.ik.complex;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
	
	List<RoomType> findByComplex(Complex complex);
	
    boolean existsByComplexAndAreaAndDepositAndMonthlyRent(
        Complex complex, String area, Integer deposit, Integer monthlyRent
    );
}