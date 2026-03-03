package org.doit.ik.user;

public interface FindPasswordService {
    // 임시 비번 발급용
    void resetAndSendPassword(String email);

    // 회원가입용 인증번호 발송
    void sendVerificationCode(String email);

    // 인증코드 검증용
    boolean checkVerificationCode(String email, String code);
}