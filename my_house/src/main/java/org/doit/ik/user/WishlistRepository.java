package org.doit.ik.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    long countByUser_Uid(Long uid);

    List<Wishlist> findByUser_UidOrderByCreatedAtDesc(Long uid);
}