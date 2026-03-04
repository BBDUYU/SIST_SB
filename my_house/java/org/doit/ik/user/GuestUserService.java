package org.doit.ik.user;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuestUserService {

    private final UserRepository userRepository;

    // 앱 시작 시 1회 만들어두고 계속 재사용
    public User getOrCreateGuest() {
        return userRepository.findByEmail("guest@myhouse.local")
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("게스트");
                    u.setNickname("게스트");
                    u.setEmail("guest@myhouse.local");
                    u.setPhone("000-0000-0000");
                    u.setPassword(null);
                    u.setProvider(Provider.LOCAL);
                    u.setProviderId(null);
                    u.setRole("ROLE_GUEST");
                    u.setCreatedAt(LocalDateTime.now());
                    u.setStatus(UserStatus.ACTIVE);
                    return userRepository.save(u);
                });
    }
}