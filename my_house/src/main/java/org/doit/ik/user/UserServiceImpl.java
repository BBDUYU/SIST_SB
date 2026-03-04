package org.doit.ik.user;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * [일반 이메일 회원가입]
     */
    @Override
    @Transactional
    public Long registerEmail(UserSignupForm form) {
        // 1) 비밀번호 확인
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 2) 이메일 중복 체크
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 3) 유저 생성 (닉네임 자동 생성)
        User user = new User();
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        
        // "사용자" 기반 랜덤 닉네임 부여 (예: 사용자_a1b2c3)
        user.setNickname(resolveUniqueNickname("사용자"));

        user.setProvider(Provider.LOCAL);
        user.setRole("ROLE_USER");
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return saved.getUid();
    }

    /**
     * [소셜 로그인 가입 및 로드]
     */
    @Override
    @Transactional
    public User registerOrLoadSocial(Provider provider, String providerId, String email, String name) {
        return userRepository.findByProviderAndProviderId(provider, providerId)
            .orElseGet(() -> {
                User user = new User();
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setEmail(email);
                user.setName(name);

                // 소셜 이름 기반 랜덤 닉네임 부여 (예: 홍길동_d4e5f6)
                user.setNickname(resolveUniqueNickname(name));

                user.setRole("ROLE_USER");
                user.setStatus(UserStatus.ACTIVE);
                user.setCreatedAt(LocalDateTime.now());
                return userRepository.save(user);
            });
    }

    /**
     * [휴대폰 번호 업데이트] - 직방 방식 추가 단계용
     */
    @Override
    @Transactional
    public void updatePhone(String email, String phone) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 숫자만 추출하여 저장
        user.setPhone(phone.replaceAll("[^0-9]", ""));
        userRepository.save(user);
    }

    /**
     * [유니크 닉네임 생성 로직]
     */
    private String resolveUniqueNickname(String baseName) {
        String cleanName = (baseName == null || baseName.isBlank()) ? "user" : baseName.trim();
        
        for (int i = 0; i < 5; i++) {
            String candidate = cleanName + "_" + UUID.randomUUID().toString().substring(0, 6);
            if (!userRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }
        return cleanName + "_" + (System.currentTimeMillis() % 1000000);
    }

   
    @Override
    @Transactional
    public void withdrawCurrentUser(Authentication auth) {
        if (auth == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        String email = null;

       
        if (auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) auth.getPrincipal();
            email = oAuth2User.getAttribute("email");
        } 
        
       
        if (email == null) {
            email = auth.getName();
        }

       
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalStateException("사용자 이메일을 찾을 수 없습니다.");
        }

        
        userRepository.deleteByEmail(email.trim());
    }

    @Override
    @Transactional
    public void withdraw(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        processWithdraw(user);
    }

    private void processWithdraw(User user) {
        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}