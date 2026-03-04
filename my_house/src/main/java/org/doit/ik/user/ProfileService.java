package org.doit.ik.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updateProfile(String email, ProfileUpdateRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 없음: " + email));

        // 1) 기본정보 업데이트
        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getPhone() != null) user.setPhone(req.getPhone());

        // 2) 비밀번호 변경 요청이 있는 경우만 처리
        boolean wantsPwChange = req.getNewPassword() != null && !req.getNewPassword().isBlank()
                             || req.getConfirmPassword() != null && !req.getConfirmPassword().isBlank()
                             || req.getCurrentPassword() != null && !req.getCurrentPassword().isBlank();

        if (wantsPwChange) {
            if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
            }

            if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
                throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
            }

            if (!req.getNewPassword().equals(req.getConfirmPassword())) {
                throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
            }

            if (req.getNewPassword().length() < 8 || req.getNewPassword().length() > 20) {
                throw new IllegalArgumentException("비밀번호는 8~20자로 입력해주세요.");
            }

            // TODO: 영문/숫자/특수문자 조합 정규식 검증도 원하면 추가해드릴게요.

            user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }

        // @Transactional이라 save 생략 가능(영속 상태면 flush됨). 안전하게 하려면:
        userRepository.save(user);
    }
}
