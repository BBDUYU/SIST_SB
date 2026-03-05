package org.doit.ik.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class LoginViewController {

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        // 1. 세션에서 에러 메시지 확인
        HttpSession session = request.getSession();
        Object errorMsg = session.getAttribute("LOGIN_ERROR_MSG");

        if (errorMsg != null) {
            // 2. 모델에 담아서 HTML(Thymeleaf)로 전달
            model.addAttribute("loginErrorMsg", errorMsg.toString());
            
            // 3. ✅ [중요] 한 번 꺼냈으면 세션에서 즉시 삭제 (1회용 메시지)
            session.removeAttribute("LOGIN_ERROR_MSG");
            
            System.out.println(">>> [CONTROLLER] 세션 에러 메시지 모델 전달 후 삭제: " + errorMsg);
        }

        return "user/login";   
    }
}