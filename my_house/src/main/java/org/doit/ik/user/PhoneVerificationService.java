package org.doit.ik.user;

import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PhoneVerificationService {

    // 메모리 저장소 (실제 운영 시에는 Redis 추천)
    private final ConcurrentHashMap<String, String> verificationStorage = new ConcurrentHashMap<>();

    // 1. 4자리 인증번호 생성 및 발송
    public void sendVerificationCode(String phone) {
        String code = String.format("%04d", new Random().nextInt(10000));
        
        // 서버 콘솔에 출력 (개발용)
        System.out.println("========================================");
        System.out.println("[SMS 발송] 번호: " + phone + " | 인증번호: " + code);
        System.out.println("========================================");
        
        verificationStorage.put(phone, code);
        // 유효시간 설정 로직 등을 추가할 수 있습니다.
    }

    // 2. 인증번호 검증
    public boolean verifyCode(String phone, String inputCode) {
        String savedCode = verificationStorage.get(phone);
        if (savedCode != null && savedCode.equals(inputCode)) {
            verificationStorage.remove(phone); // 인증 성공 시 삭제
            return true;
        }
        return false;
    }
}