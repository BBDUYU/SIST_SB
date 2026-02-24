package org.doit.ik.complex;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    boolean existsByComplexAndAreaAndDepositAndMonthlyRent(
        Complex complex, String area, Integer deposit, Integer monthlyRent
    );
}