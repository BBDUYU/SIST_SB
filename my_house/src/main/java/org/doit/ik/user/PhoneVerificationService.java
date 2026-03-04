package org.doit.ik.user;

import java.util.Random;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final PhoneAuthRepository phoneAuthRepository; 
    private final SmsService smsService; 

    
    @Transactional
    public void sendVerificationCode(String phone) {
       
        String code = String.format("%06d", new Random().nextInt(1000000));
        
       
        System.out.println("[회원가입 SMS 발송] 번호: " + phone + " | 인증번호: " + code);
        
        
        PhoneAuth auth = new PhoneAuth(phone, code);
        phoneAuthRepository.save(auth);

       
        smsService.sendSms(phone, "[MyHouse] 회원가입 인증번호: " + code);
    }

    // 2. DB에서 가장 최근 인증번호를 꺼내어 검증
    @Transactional(readOnly = true)
    public boolean verifyCode(String phone, String inputCode) {
        return phoneAuthRepository.findTopByPhoneOrderByCreatedAtDesc(phone)
            .map(auth -> auth.getCode().equals(inputCode))
            .orElse(false);
    }
}