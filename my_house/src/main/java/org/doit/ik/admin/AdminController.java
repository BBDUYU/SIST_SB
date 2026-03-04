package org.doit.ik.admin;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

  @GetMapping("/admin")
  public String adminHome(Model model, Principal principal) {
    model.addAttribute("isLogin", principal != null);
    return "admin/dashboard";
  }

  @GetMapping("/admin/members")
  public String members(Model model, Principal principal) {
    model.addAttribute("isLogin", principal != null);
    // TODO: members 데이터 model.addAttribute("members", ...)
    return "admin/members";
  }

  @GetMapping("/admin/reviews")
  public String reviews(Model model, Principal principal) {
    model.addAttribute("isLogin", principal != null);
    // TODO: reviews 데이터 model.addAttribute("reviews", ...)
    return "admin/reviews";
  }

  @GetMapping("/admin/qna")
  public String qna(Model model, Principal principal) {
    model.addAttribute("isLogin", principal != null);
    // TODO: qna 데이터 model.addAttribute("questions", ...)
    return "admin/qna";
  }
}