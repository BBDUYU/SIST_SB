package org.doit.ik.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneAuthRepository extends JpaRepository<PhoneAuth, Long> {
    Optional<PhoneAuth> findTopByPhoneOrderByCreatedAtDesc(String phone);
}