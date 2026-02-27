package org.doit.ik.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mypage")
public class MyPageController {
	
	@Value("${kakao.maps.js-key}")
    private String kakaoJsKey;

    @GetMapping({"", "/"})
    public String mypage(Model model, Principal principal) {

    	model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);

        // user
        Map<String, Object> user = new HashMap<>();
        user.put("name", "김철수");
        user.put("grade", "VIP");
        user.put("email", "chulsoo.kim@email.com");
        user.put("phone", "010-1234-5678");
        user.put("address", "서울특별시 강남구 테헤란로 123");

        // stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("recentView", 24);
        stats.put("reviewCount", 8);
        stats.put("inquiryCount", 3);

        // properties
        List<Map<String, Object>> properties = new ArrayList<>();
        Map<String, Object> p1 = new HashMap<>();
        p1.put("imageUrl", "/images/sample/property1.jpg");
        p1.put("title", "강남 신축 오피스텔");
        p1.put("location", "서울 강남구");
        p1.put("type", "매매");
        p1.put("price", "3억 2,000만원");
        p1.put("viewTime", "2시간 전");
        properties.add(p1);

        // reviewStats
        Map<String, Object> reviewStats = new HashMap<>();
        reviewStats.put("averageScore", 4.7);

        // reviews
        List<Map<String, Object>> reviews = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>();
        r1.put("propertyName", "강남 신축 오피스텔");
        r1.put("rating", 5);
        r1.put("date", "2026.02.15");
        r1.put("content", "교통이 편리하고 깔끔합니다.");
        r1.put("likes", 12);
        r1.put("comments", 3);
        reviews.add(r1);

        // inquiries
        List<Map<String, Object>> inquiries = new ArrayList<>();

        Map<String, Object> i1 = new HashMap<>();
        i1.put("title", "계약 조건 문의드립니다");
        i1.put("status", "answered");
        i1.put("propertyName", "강남 신축 오피스텔");
        i1.put("date", "2026.02.20");
        i1.put("question", "보증금 대출 가능 여부 문의");
        i1.put("answer", "가능합니다.");
        inquiries.add(i1);

        Map<String, Object> i2 = new HashMap<>();
        i2.put("title", "관리비 포함 내역 문의");
        i2.put("status", "pending");
        i2.put("propertyName", "역세권 원룸");
        i2.put("date", "2026.02.22");
        i2.put("question", "인터넷 포함인가요?");
        i2.put("answer", null);
        inquiries.add(i2);

        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        model.addAttribute("properties", properties);
        model.addAttribute("reviewStats", reviewStats);
        model.addAttribute("reviews", reviews);
        model.addAttribute("inquiries", inquiries);

        return "/mypage/mypage";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {

    	model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);

        return "/mypage/profile";
    }
    
    @GetMapping("/inquiry")
    public String inquiry(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);

        // 필요하면 화면에서 기본값도 내려줄 수 있음(선택)
        // model.addAttribute("createdDate", LocalDate.now().toString());

        return "/mypage/inquiry"; // 템플릿 경로에 맞춰 변경
    }
}