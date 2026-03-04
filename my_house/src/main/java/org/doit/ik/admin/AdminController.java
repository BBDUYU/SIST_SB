package org.doit.ik.admin;

import java.security.Principal;
import org.doit.ik.user.UserStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // 서비스 주입을 위해 필수
public class AdminController {
    
    private final AdminUserService adminUserService;

    @GetMapping("/admin/members")
    public String members(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("members", adminUserService.getAllMembers());
        return "admin/members";
    }

    @PostMapping("/admin/members/update-status")
    @ResponseBody
    public String updateStatus(@RequestParam("userId") Long userId, @RequestParam("status") UserStatus status) {
        adminUserService.updateUserStatus(userId, status);
        return "success";
    }

    @PostMapping("/admin/members/update-role")
    @ResponseBody
    public String updateRole(@RequestParam("userId") Long userId, @RequestParam("role") String role) { // ✅ String으로 변경
        adminUserService.updateUserRole(userId, role);
        return "success";
    }

    @GetMapping("/admin/members/register")
    public String registerForm(Model model) {
        
        return "admin/register"; 
    }
    
    @GetMapping("/admin/reviews")
    public String reviews(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        return "admin/reviews";
    }
}