package org.doit.ik.user;

import java.util.List;

import org.doit.ik.user.Review.ReviewStatus; // static 대신 타입으로 import
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUser_UidAndDeletedAtIsNullOrderByCreatedAtDesc(Long uid);
    long countByUser_UidAndDeletedAtIsNull(Long uid);
    
    boolean existsByComplex_CidAndUser_UidAndStatus(Long cid, Long uid, Review.ReviewStatus status);

    List<Review> findByComplex_CidAndStatusOrderByCreatedAtDesc(Long cid, ReviewStatus status);
    long countByComplex_CidAndStatus(Long cid, ReviewStatus status);

    @Query("select avg(r.rating) from Review r where r.complex.cid = :cid and r.status = 'ACTIVE'")
    Double getAvgRatingByComplex(@Param("cid") Long cid);

    @Query("select avg(r.rating) from Review r where r.user.uid = :uid and r.deletedAt is null")
    Double avgRating(@Param("uid") Long uid);
    
    // 관리자 페이지 리뷰 목록
    @EntityGraph(attributePaths = {"user", "complex"})
    Page<Review> findByStatusOrderByCreatedAtDesc(Review.ReviewStatus status, Pageable pageable);
    
    // 관리자 페이지 리뷰 목록 검색
    @EntityGraph(attributePaths = {"user", "complex"})
    @Query("""
    select r from Review r
    left join r.complex c
    left join r.user u
    where r.status = :status
      and (
        :q is null or :q = '' or
        c.title like %:q% or
        c.address like %:q% or
        u.nickname like %:q% or
        r.title like %:q% or
        r.content like %:q%
      )
    order by r.createdAt desc
    """)
    Page<Review> searchByStatus(@Param("status") Review.ReviewStatus status,
                               @Param("q") String q,
                               Pageable pageable);
    
    
}