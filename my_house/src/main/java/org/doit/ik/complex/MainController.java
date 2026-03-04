package org.doit.ik.complex;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.doit.ik.user.Provider; // Provider Enum 위치 확인 필요
import org.doit.ik.user.Review;
import org.doit.ik.user.ReviewRepository;
import org.doit.ik.user.User;
import org.doit.ik.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    // 1. 공통 사용자 조회 (MyPageController 로직 복사)
    private User currentUser(Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Object p = auth.getPrincipal();

        // 일반 로그인 처리
        if (p instanceof UserDetails ud) {
            return userRepository.findByEmail(ud.getUsername()).orElse(null);
        }

        // OAuth2(소셜) 로그인 처리
        if (p instanceof OAuth2User ou) {
            // (A) email 우선 확인
            Object emailObj = ou.getAttribute("email");
            if (emailObj instanceof String email && !email.isBlank()) {
                return userRepository.findByEmail(email).orElse(null);
            }

            // (B) 카카오 전용 계정 정보 확인
            Object ka = ou.getAttribute("kakao_account");
            if (ka instanceof Map<?, ?> kakaoAccount) {
                Object kEmail = kakaoAccount.get("email");
                if (kEmail instanceof String email && !email.isBlank()) {
                    return userRepository.findByEmail(email).orElse(null);
                }
            }

            // (C) ProviderId 기반 확인
            if (auth instanceof OAuth2AuthenticationToken oat) {
                try {
                    Provider provider = Provider.valueOf(oat.getAuthorizedClientRegistrationId().toUpperCase());
                    Object idObj = ou.getAttribute("id");
                    if (idObj != null) {
                        return userRepository.findByProviderAndProviderId(provider, String.valueOf(idObj)).orElse(null);
                    }
                } catch (Exception e) {}
            }
            return userRepository.findByEmail(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping("/main")
    public String main(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);
        return "main/main";
    }

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
        // ✅ Principal을 직접 쓰지 않고 검증된 currentUser() 사용
        User user = currentUser(principal);

        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 중복 리뷰 체크
        boolean exists = reviewRepository.existsByComplex_CidAndUser_UidAndStatus(
                cid, user.getUid(), Review.ReviewStatus.ACTIVE);
        
        if (exists) {
            return ResponseEntity.status(409).body("이미 해당 매물에 리뷰를 작성하셨습니다.");
        }

        Complex complex = complexRepository.findById(cid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매물입니다."));

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