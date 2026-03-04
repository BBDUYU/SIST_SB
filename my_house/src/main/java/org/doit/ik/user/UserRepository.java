package org.doit.ik.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ 활성 계정(탈퇴 안 한 계정) 기준 이메일 조회
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // ✅ 아이디 찾기(휴대폰 등록한 사용자만 조회됨)
    Optional<User> findByPhoneAndDeletedAtIsNull(String phone);
    
    void deleteByEmail(String email);

    // ✅ 중복 체크
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    // ✅ 추후 회원정보 수정에서 phone 받을 거면 유지
    boolean existsByPhone(String phone);

    // ✅ 소셜 고유 식별자 조회
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
    
    Optional<User> findByEmail(String email);
}