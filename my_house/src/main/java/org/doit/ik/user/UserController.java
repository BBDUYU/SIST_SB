package org.doit.ik.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FindIdService findIdService;
    private final FindPasswordService findPasswordService;
    private final PhoneVerificationService phoneVerificationService;

    // =========================
    // 회원가입 (GET)
    // =========================
    @GetMapping("/signup")
    public String signupPage(UserSignupForm form) {
        return "user/signup";
    }

    // =========================
    // 회원가입 (POST) : 중복 해결 및 자동 로그인 통합
    // =========================
    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signupSubmit(@Valid @RequestBody UserSignupForm form, 
                                          BindingResult bindingResult, 
                                          HttpServletRequest request) {

        // 1. 유효성 검사 (비번 일치 및 필수 약관 동의)
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "passwordMismatch", "비밀번호가 일치하지 않습니다.");
        }
        if (!form.isAgreeTerms()) {
            bindingResult.rejectValue("agreeTerms", "required", "이용약관 동의는 필수입니다.");
        }
        if (!form.isAgreePrivacy()) {
            bindingResult.rejectValue("agreePrivacy", "required", "개인정보 처리방침 동의는 필수입니다.");
        }

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(firstError);
        }

        try {
            // 2. 가입 실행
            userService.registerEmail(form);

            // 3. 자동 로그인 처리 (setup-phone 접근 권한 확보)
            try {
                request.login(form.getEmail(), form.getPassword());
            } catch (ServletException e) {
                // 자동 로그인 실패 시에도 가입은 성공한 것이므로 200 응답
                return ResponseEntity.ok("회원가입 성공 (자동 로그인 실패)");
            }

            return ResponseEntity.ok("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================
    // 휴대폰 등록 (직방 방식 Step 1 & 2)
    // =========================
    @GetMapping("/setup-phone")
    public String setupPhoneForm() {
        return "user/setup-phone";
    }

    @GetMapping("/verify-phone")
    public String verifyPhoneForm(@RequestParam("phone") String phone, Model model) {
        model.addAttribute("phone", phone); 
        return "user/verify-phone";
    }


    @PostMapping("/api/send-sms")
    @ResponseBody
    public ResponseEntity<?> sendSms(@RequestParam("phone") String phone) { 
    	System.out.println("컨트롤러 진입 성공");
    	try {
            phoneVerificationService.sendVerificationCode(phone);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/api/verify-phone")
    @ResponseBody
    public ResponseEntity<?> verifySms(@RequestBody PhoneVerificationRequest request, 
                                      Authentication auth) {
        // DTO에서 데이터를 꺼내서 사용
        if (phoneVerificationService.verifyCode(request.getPhone(), request.getCode())) {
            userService.updatePhone(auth.getName(), request.getPhone());
            return ResponseEntity.ok("Success");
        }
        return ResponseEntity.badRequest().body("인증번호가 일치하지 않습니다.");
    }

    // =========================
    // 아이디 찾기 관련
    // =========================
    @GetMapping("/find-id")
    public String findIdPage() {
        return "user/find-id";
    }

    @PostMapping("/find-id")
    public String sendFindIdCode(@RequestParam("phone") String phone, 
                                 RedirectAttributes redirectAttributes) {
        try {
            findIdService.sendCode(phone); 
            redirectAttributes.addFlashAttribute("phone", phone);
            return "redirect:/user/find-id-verify"; 
        } catch (Exception e) {
        	System.out.println("아이디 찾기 실패 원인: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/find-id";
        }
    }

    @GetMapping("/find-id-verify")
    public String findVerifyPage(@ModelAttribute("phone") String phone, 
                                 @ModelAttribute("error") String error) {
        if (phone == null || phone.isEmpty()) return "redirect:/user/find-id";
        return "user/find-id-verify";
    }

    @PostMapping("/find-id-verify")
    public String verifyFindIdCode(@RequestParam("phone") String phone, 
                                   @RequestParam("code") String code, 
                                   RedirectAttributes redirectAttributes) {
        try {
            String maskedEmail = findIdService.verifyCode(phone, code); 
            redirectAttributes.addFlashAttribute("email", maskedEmail);
            return "redirect:/user/find-id-result"; 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("phone", phone);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/find-id-verify";
        }
    }

    @GetMapping("/find-id-result")
    public String findResultPage(@ModelAttribute("email") String email) {
        if (email == null || email.isEmpty()) return "redirect:/user/find-id";
        return "user/find-id-result";
    }

    // =========================
    // 비밀번호 찾기 및 탈퇴
    // =========================
    @GetMapping("/find-password")
    public String findPasswordPage() {
        return "user/find-password";
    }
    
    @PostMapping("/find-password")
    public String processFindPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            findPasswordService.resetAndSendPassword(email);
            redirectAttributes.addFlashAttribute("message", "이메일로 임시 비밀번호가 발급되었습니다.");
            return "redirect:/user/login?sent=true"; 
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/find-password";
        }
    }

    @GetMapping("/withdraw")
    public String withdrawPage() {
        return "user/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdrawSubmit(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userService.withdrawCurrentUser(auth);
        new SecurityContextLogoutHandler().logout(request, response, auth);
        return "redirect:/main?withdraw=success";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/main";
    }
}