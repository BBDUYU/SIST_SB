package org.doit.ik.user;

public interface EmailService {
	// 비밀번호 재발급
    void sendTemporaryPassword(String toEmail, String tempPassword);
    // 회원가입 이메일 인증
    void sendVerificationCode(String toEmail, String code);
}