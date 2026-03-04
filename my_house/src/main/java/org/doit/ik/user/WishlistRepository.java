package org.doit.ik.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    long countByUser_Uid(Long uid);

    List<Wishlist> findByUser_UidOrderByCreatedAtDesc(Long uid);

    boolean existsByUser_UidAndComplex_Cid(Long uid, Long cid);

    Optional<Wishlist> findByUser_UidAndComplex_Cid(Long uid, Long cid);
    
    @org.springframework.data.jpa.repository.Query(
    	"select w.complex.cid from Wishlist w where w.user.uid = :uid"
    )
    List<Long> findCidListByUid(@org.springframework.data.repository.query.Param("uid") Long uid);
    
}