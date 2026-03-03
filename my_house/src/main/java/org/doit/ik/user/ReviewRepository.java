package org.doit.ik.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUser_UidAndDeletedAtIsNullOrderByCreatedAtDesc(Long uid);

    long countByUser_UidAndDeletedAtIsNull(Long uid);

    @Query("select avg(r.rating) from Review r where r.user.uid = :uid and r.deletedAt is null")
    Double avgRating(@Param("uid") Long uid);
}