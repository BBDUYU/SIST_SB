package org.doit.ik.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class LoginViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";   
    }
}