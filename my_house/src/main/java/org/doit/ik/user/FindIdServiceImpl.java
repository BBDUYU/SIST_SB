package org.doit.ik.user;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindIdServiceImpl implements FindIdService {

    private final SmsService smsService;
    private final PhoneAuthRepository phoneAuthRepository;
    private final UserRepository userRepository;

    @Override
    public void sendCode(String phone) {

        String code = String.valueOf((int)((Math.random() * 900000) + 100000));

        phoneAuthRepository.save(new PhoneAuth(phone, code));

        smsService.sendSms(phone, "[아이디찾기] 인증번호: " + code);
    }

    @Override
    public String verifyCode(String phone, String code) {

        PhoneAuth auth = phoneAuthRepository
                .findTopByPhoneOrderByCreatedAtDesc(phone)
                .orElseThrow();

        if (!auth.getCode().equals(code)) {
            throw new RuntimeException("인증번호 불일치");
        }

     // FindIdServiceImpl.java 내부 수정
        User user = userRepository.findByPhoneAndDeletedAtIsNull(phone)
                .orElseThrow(() -> new RuntimeException("해당 번호로 가입된 아이디가 없습니다."));

        return maskEmail(user.getEmail());
    }

    private String maskEmail(String email) {
        int idx = email.indexOf("@");
        return email.substring(0, 2) + "****" + email.substring(idx);
    }
}