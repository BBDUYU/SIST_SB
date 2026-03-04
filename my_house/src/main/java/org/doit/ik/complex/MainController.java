package org.doit.ik.complex;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.doit.ik.user.Review;
import org.doit.ik.user.ReviewRepository;
import org.doit.ik.user.User;
import org.doit.ik.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final ReviewRepository reviewRepository;
    private final ComplexRepository complexRepository;
    private final UserRepository userRepository;
	
    @Value("${kakao.maps.js-key}")
    private String kakaoJsKey;

    @GetMapping("/main")
    public String main(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);
        return "main/main";
    }
/*
    @GetMapping("/listing/{propertyKey}")
    public String detail(@PathVariable("propertyKey") String propertyKey,
                         Model model,
                         Principal principal) {

        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);

        Map<String, Object> property = dummyProperty(propertyKey);
        model.addAttribute("property", property);

        return "listing/detail";
    }
*/
    // ✅ panel: detail.html 안에 정의한 fragment만 반환
    /*
    @GetMapping("/listing/{propertyKey}/panel")
    public String detailPanel(@PathVariable("propertyKey") String propertyKey,
                              Model model,
                              Principal principal) {

        model.addAttribute("isLogin", principal != null);

        Map<String, Object> property = dummyProperty(propertyKey);
        model.addAttribute("property", property);

        return "listing/detail_panel :: panel";
    }

    private Map<String, Object> dummyProperty(String propertyKey) {
        Map<String, Object> property = new HashMap<>();
        property.put("key", propertyKey);
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
        return property;
    }*/
    @GetMapping("/api/reviews/{cid}")
    @ResponseBody
    public ResponseEntity<?> getReviews(@PathVariable("cid") Long cid) {
    	List<Review> reviews = reviewRepository.findByComplex_CidAndStatusOrderByCreatedAtDesc(cid, Review.ReviewStatus.ACTIVE);
        
        Double avg = reviewRepository.getAvgRatingByComplex(cid);
        long count = reviewRepository.countByComplex_CidAndStatus(cid, Review.ReviewStatus.ACTIVE);

        Map<String, Object> response = new HashMap<>();
        response.put("avgScore", avg != null ? Math.round(avg * 10) / 10.0 : 0.0);
        response.put("reviewCount", count);
        response.put("list", reviews.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("nickname", r.getUser() != null ? r.getUser().getNickname() : "익명");
            map.put("rating", r.getRating());
            map.put("content", r.getContent());
            map.put("date", r.getCreatedAt() != null ? r.getCreatedAt().toLocalDate().toString() : "");
            return map;
        }).toList());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/api/reviews/{cid}")
    @ResponseBody
    public ResponseEntity<?> addReview(@PathVariable("cid") Long cid, 
                                       @RequestBody Map<String, Object> payload,
                                       Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Complex complex = complexRepository.findById(cid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매물입니다."));
        
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        Review review = new Review();
        review.setComplex(complex);
        review.setUser(user);
        review.setRating(Integer.parseInt(payload.get("rating").toString()));
        review.setContent(payload.get("content").toString());
        review.setCreatedAt(java.time.LocalDateTime.now());
        
        review.setStatus(Review.ReviewStatus.ACTIVE); 

        reviewRepository.save(review);
        
        return ResponseEntity.ok().build();
    }
}
