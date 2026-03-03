package org.doit.ik.user;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindPasswordServiceImpl implements FindPasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthVerificationRepository authVerificationRepository;

    @Override
    @Transactional
    public void resetAndSendPassword(String email) {
        // 1. 유저 존재 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일 주소입니다."));

        // 2. 임시 비밀번호 생성 (8자리)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // 3. DB 비밀번호 업데이트 (암호화 필수)
        user.setPassword(passwordEncoder.encode(tempPassword));
        
        // 4. 이메일 발송
        emailService.sendTemporaryPassword(email, tempPassword);
    }
    
    /**
     * 추가 로직 1: 회원가입용 인증번호 생성 및 발송
     */
    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        // 1. 기존에 해당 이메일로 발송된 미사용 인증번호가 있다면 무효화(선택사항)
        // 2. 새 번호 생성
        String code = String.valueOf((int)(Math.random() * 899999) + 100000);

        AuthVerification auth = new AuthVerification();
        auth.setUid(0L); 
        auth.setVerifyType(VerifyType.SIGNUP_EMAIL); 
        auth.setChannel(VerifyChannel.EMAIL);
        auth.setTargetValue(email);
        auth.setCode(code);
        auth.setExpiresAt(LocalDateTime.now().plusMinutes(3));
        auth.setUsedYn("N");

        authVerificationRepository.save(auth);
        emailService.sendVerificationCode(email, code);
    }

    /**
     * 추가 로직 2: 인증번호 검증
     */
    @Override
    @Transactional
    public boolean checkVerificationCode(String email, String code) {
        Optional<AuthVerification> authOpt = authVerificationRepository
            .findTopByVerifyTypeAndChannelAndTargetValueAndCodeAndUsedYnFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                VerifyType.SIGNUP_EMAIL, // ✅ SIGNUP -> SIGNUP_EMAIL 로 수정
                VerifyChannel.EMAIL,
                email,
                code,
                LocalDateTime.now()
            );

        if (authOpt.isPresent()) {
            AuthVerification auth = authOpt.get();
            auth.setUsedYn("Y"); 
            return true;
        }
        return false;
    }
}