package org.doit.ik.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    Optional<Terms> findByTermsCode(String termsCode); // 예: "TERMS", "PRIVACY", "MARKETING"

    boolean existsByTermsCode(String termsCode);
}