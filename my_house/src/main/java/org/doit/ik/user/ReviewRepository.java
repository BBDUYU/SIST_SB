package org.doit.ik.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.doit.ik.user.Review.ReviewStatus; // static 대신 타입으로 import

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUser_UidAndDeletedAtIsNullOrderByCreatedAtDesc(Long uid);
    long countByUser_UidAndDeletedAtIsNull(Long uid);

    List<Review> findByComplex_CidAndStatusOrderByCreatedAtDesc(Long cid, ReviewStatus status);
    long countByComplex_CidAndStatus(Long cid, ReviewStatus status);

    @Query("select avg(r.rating) from Review r where r.complex.cid = :cid and r.status = 'ACTIVE'")
    Double getAvgRatingByComplex(@Param("cid") Long cid);

    @Query("select avg(r.rating) from Review r where r.user.uid = :uid and r.deletedAt is null")
    Double avgRating(@Param("uid") Long uid);
}