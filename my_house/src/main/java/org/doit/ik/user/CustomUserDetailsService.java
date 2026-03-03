package org.doit.ik.user;

import java.util.Collections;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * formLogin에서 usernameParameter("email")로 넘어온 값을 여기서 받는다.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("이메일이 비어있습니다.");
        }

        String normalized = email.trim().toLowerCase();

        // soft delete 정책
        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // deletedAt 기반 차단
        if (user.getDeletedAt() != null) {
            throw new DisabledException("탈퇴한 사용자입니다.");
        }

        // status 기반 차단 (있다면 유지)
        if (user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("비활성 사용자입니다.");
        }

        // LOCAL만 formLogin 허용
        if (user.getProvider() != null && user.getProvider() != Provider.LOCAL) {
            throw new DisabledException("소셜 가입 계정입니다. 소셜 로그인으로 진행하세요.");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new DisabledException("비밀번호가 설정되지 않은 계정입니다.");
        }

        String role = (user.getRole() == null || user.getRole().isBlank())
                ? "ROLE_USER"
                : user.getRole().trim();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(role))
        );
    }
    

}