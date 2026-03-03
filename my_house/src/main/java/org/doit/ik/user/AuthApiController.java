package org.doit.ik.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final FindPasswordService findPasswordService; // 기존 서비스 활용

    /**
     * 1. 회원가입용 이메일 인증번호 발송
     * JS의 '인증하기' 버튼 클릭 시 호출
     */
    @PostMapping("/email-send")
    public ResponseEntity<String> sendSignupCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            // FindPasswordService에 인증번호 생성 및 발송 로직 추가 필요
            findPasswordService.sendVerificationCode(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("발송 실패: " + e.getMessage());
        }
    }

    /**
     * 2. 인증번호 일치 여부 확인
     * JS의 '확인' 버튼 클릭 시 호출
     */
    @PostMapping("/email-verify")
    public ResponseEntity<String> verifySignupCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        // DB에 저장된 번호와 일치하는지 확인
        boolean isMatch = findPasswordService.checkVerificationCode(email, code);

        if (isMatch) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(401).body("인증번호가 일치하지 않습니다.");
        }
    }
}