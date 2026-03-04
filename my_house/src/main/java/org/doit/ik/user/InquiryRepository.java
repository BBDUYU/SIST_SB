package org.doit.ik.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    long countByUser_UidAndDeletedFalse(Long uid);

    List<Inquiry> findByUser_UidAndDeletedFalseOrderByCreatedAtDesc(Long uid);
    
    List<Inquiry> findByDeletedFalseOrderByCreatedAtDesc();

    List<Inquiry> findByStatusAndDeletedFalseOrderByCreatedAtDesc(InquiryStatus status);
    
    List<Inquiry> findByTitleContainingOrContentContainingOrUser_NicknameContainingAndDeletedFalse(
            String title, String content, String nickname);
    
}