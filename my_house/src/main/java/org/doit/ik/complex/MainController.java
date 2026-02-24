package org.doit.ik.complex;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/main")
    public String main(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);

        // 일단 화면만 띄우려면 houses 없어도 됨(템플릿에 더미가 뜸)
        // model.addAttribute("houses", List.of()); // 있어도 되고 없어도 됨

        model.addAttribute("alarmCount", 3); // 없으면 배지 안뜸
        return "/main/main";
    }
}