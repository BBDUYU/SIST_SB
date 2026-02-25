package org.doit.ik.complex;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MainController {

    @Value("${kakao.maps.js-key}")
    private String kakaoJsKey;

    @GetMapping("/main")
    public String main(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);
        return "main/main";
    }

    @GetMapping("/listing/{propertyKey}")
    public String detail(@PathVariable("propertyKey") String propertyKey,
                         Model model,
                         Principal principal) {

        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);

        Map<String, Object> property = new HashMap<>();
        property.put("name", "서울 관악구 신림동 원룸");
        property.put("price", "보증금 1000 / 월세 45");
        property.put("type", "원룸");
        property.put("area", "18㎡");
        property.put("address", "서울특별시 관악구 신림동 123-45");
        property.put("lat", 37.501824);
        property.put("lng", 126.9476234);
        property.put("safetyScore", 78);
        property.put("cctvCount", 21);
        property.put("safeRoadDistance", 320);

        model.addAttribute("property", property);

        return "listing/detail";
    }
}