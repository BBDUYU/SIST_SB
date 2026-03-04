package org.doit.ik.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {

    // 특정 유저의 동의 내역
    List<UserTerms> findAllByUser(User user);

    // ✅ 특정 유저가 특정 약관에 동의했는지 (Terms PK 필드명에 맞게 변경)
    Optional<UserTerms> findByUserUidAndTermsTermsId(Long uid, Long termsId);

    // 특정 유저 동의 내역 삭제(탈퇴 시 정리하고 싶다면)
    void deleteAllByUserUid(Long uid);

    // ✅ 존재 여부로도 체크 가능
    boolean existsByUserUidAndTermsTermsId(Long uid, Long termsId);
}