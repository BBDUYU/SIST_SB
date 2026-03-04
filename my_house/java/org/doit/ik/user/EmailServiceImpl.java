package org.doit.ik.user;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendTemporaryPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[내집마련] 임시 비밀번호가 발급되었습니다.");
        message.setText("안녕하세요. 요청하신 임시 비밀번호는 [" + tempPassword + "] 입니다.\n로그인 후 반드시 비밀번호를 변경해 주세요.");
        mailSender.send(message);
    }
    
    @Override
    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[내집마련] 회원가입 인증번호 안내");
        message.setText("안녕하세요. 회원가입을 위한 인증번호는 [" + code + "] 입니다.\n" +
                       "3분 이내에 인증을 완료해 주세요.");
        
        mailSender.send(message);
    }
}