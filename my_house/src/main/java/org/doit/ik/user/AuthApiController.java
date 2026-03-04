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