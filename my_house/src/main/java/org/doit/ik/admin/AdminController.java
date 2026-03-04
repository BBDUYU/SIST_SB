package org.doit.ik.admin;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

  @GetMapping("/admin/members")
  public String members(Model model, Principal principal) {
    model.addAttribute("isLogin", principal != null);
    // TODO: members 데이터 model.addAttribute("members", ...)
    return "admin/members";
  }

}