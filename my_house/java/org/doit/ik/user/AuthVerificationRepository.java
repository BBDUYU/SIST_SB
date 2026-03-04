package org.doit.ik.user;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthVerificationRepository
        extends JpaRepository<AuthVerification, Long> {

    Optional<AuthVerification>
    findTopByVerifyTypeAndChannelAndTargetValueAndCodeAndUsedYnFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            VerifyType verifyType,
            VerifyChannel channel,
            String targetValue,
            String code,
            LocalDateTime now
    );

}