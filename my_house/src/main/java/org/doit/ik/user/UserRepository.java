package org.doit.ik.user;

import java.util.Optional;

import org.doit.ik.user.Provider;
import org.doit.ik.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}