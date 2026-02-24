package org.doit.ik.api;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LawdCodeRepository extends JpaRepository<LawdCode, String> {
    
    List<LawdCode> findByIsActiveTrue();
    
    boolean existsByLawdCd(String lawdCd);
    
    Optional<LawdCode> findTopByLawdCdStartingWith(String fiveDigitCode);
}