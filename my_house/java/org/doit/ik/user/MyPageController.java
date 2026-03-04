package org.doit.ik.user;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.doit.ik.complex.RecentlyViewed; // ✅ complex 패키지 엔티티 사용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    @Value("${kakao.maps.js-key}")
    private String kakaoJsKey;

    private final UserRepository userRepository;
    private final RecentlyViewedRepository recentlyViewedRepository;
    private final ReviewRepository reviewRepository;
    private final InquiryRepository inquiryRepository;
    private final WishlistRepository wishlistRepository;

    @GetMapping({"", "/"})
    public String mypage(Model model, Principal principal) {

        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);

        // 로그인 안 된 상태면(전체허용 테스트 등)
        if (principal == null) {
            model.addAttribute("user", Map.of());
            model.addAttribute("stats", Map.of("recentView", 0, "reviewCount", 0, "inquiryCount", 0));
            model.addAttribute("properties", List.of());
            model.addAttribute("reviewStats", Map.of("averageScore", 0.0));
            model.addAttribute("reviews", List.of());
            model.addAttribute("inquiries", List.of());
            return "/mypage/mypage";
        }

        // principal.getName() = email
        String email = principal.getName();
        User loginUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 없음: " + email));

        Long uid = loginUser.getUid();

        // -----------------------
        // 1) user
        // -----------------------
        Map<String, Object> user = new HashMap<>();
        user.put("name", loginUser.getName());
        user.put("grade", loginUser.getRole());
        user.put("email", loginUser.getEmail());
        user.put("phone", loginUser.getPhone());
        user.put("address", ""); // user 테이블에 주소 없으면 빈값
        user.put("nickname", loginUser.getNickname());
        model.addAttribute("user", user);

        // -----------------------
        // 2) stats
        // -----------------------
        long recentCnt = recentlyViewedRepository.countByUser_Uid(uid);
        long reviewCnt = reviewRepository.countByUser_UidAndDeletedAtIsNull(uid);
        long inquiryCnt = inquiryRepository.countByUser_UidAndDeletedFalse(uid);

        Map<String, Object> stats = new HashMap<>();
        stats.put("recentView", recentCnt);
        stats.put("reviewCount", reviewCnt);
        stats.put("inquiryCount", inquiryCnt);
        model.addAttribute("stats", stats);

        // -----------------------
        // 3) 최근본매물(properties)
        // complex.RecentlyViewed는 cid 필드가 아니라 complex 관계임
        // -----------------------
        List<RecentlyViewed> recentRows =
                recentlyViewedRepository.findTop4ByUser_UidOrderByViewedAtDesc(uid);

        List<Map<String, Object>> properties = new ArrayList<>();
        for (RecentlyViewed rv : recentRows) {
            Map<String, Object> p = new HashMap<>();

            // 디자인 유지용 기본값
            p.put("imageUrl", "/images/sample/property1.jpg");

            Long cid = (rv.getComplex() != null ? rv.getComplex().getCid() : null);
            p.put("title", "단지ID: " + (cid != null ? cid : ""));

            p.put("location", "");
            p.put("type", "");
            p.put("price", "");
            p.put("viewTime", toAgo(rv.getViewedAt()));

            properties.add(p);
        }
        model.addAttribute("properties", properties);

        // -----------------------
        // 4) 리뷰
        // Review 엔티티는 Complex complex 관계라서 getCid() 없음
        // -----------------------
        Double avg = reviewRepository.avgRating(uid);
        double avgScore = (avg == null) ? 0.0 : Math.round(avg * 10.0) / 10.0;

        Map<String, Object> reviewStats = new HashMap<>();
        reviewStats.put("averageScore", avgScore);
        model.addAttribute("reviewStats", reviewStats);

        List<Review> reviewRows =
                reviewRepository.findByUser_UidAndDeletedAtIsNullOrderByCreatedAtDesc(uid);

        List<Map<String, Object>> reviews = new ArrayList<>();
        for (Review r : reviewRows) {
            Map<String, Object> row = new HashMap<>();
            row.put("propertyName", "단지ID: " + (r.getComplex() != null ? r.getComplex().getCid() : ""));
            row.put("rating", r.getRating());
            row.put("date", formatDate(r.getCreatedAt()));
            row.put("content", r.getContent());
            row.put("likes", 0);
            row.put("comments", 0);
            reviews.add(row);
        }
        model.addAttribute("reviews", reviews);

        // -----------------------
        // 5) 문의
        // -----------------------
        List<Inquiry> inquiryRows =
        	    inquiryRepository.findByUser_UidAndDeletedFalseOrderByCreatedAtDesc(uid);

        List<Map<String, Object>> inquiries = new ArrayList<>();
        for (Inquiry iq : inquiryRows) {
            Map<String, Object> row = new HashMap<>();
            row.put("title", iq.getTitle());
            row.put("status", "COMPLETED".equalsIgnoreCase(iq.getStatus().name()) ? "answered" : "pending");
            row.put("propertyName", "");
            row.put("date", formatDate(iq.getCreatedAt()));
            row.put("question", iq.getContent());
            row.put("answer", iq.getAnswer());
            inquiries.add(row);
        }
        model.addAttribute("inquiries", inquiries);

        return "/mypage/mypage";
    }

    // ===== util =====
    private String formatDate(LocalDateTime dt) {
        if (dt == null) return "";
        return String.format("%d.%02d.%02d", dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth());
    }

    private String toAgo(LocalDateTime dt) {
        if (dt == null) return "";
        Duration d = Duration.between(dt, LocalDateTime.now());
        long minutes = d.toMinutes();
        if (minutes < 60) return minutes + "분 전";
        long hours = d.toHours();
        if (hours < 24) return hours + "시간 전";
        long days = d.toDays();
        return days + "일 전";
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
        return "/mypage/inquiry";
    }
    
    @GetMapping("/wishlist")
    public String wishlist(Model model, Principal principal) {

        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);

        if (principal == null) {
            model.addAttribute("wishlists", List.of());
            model.addAttribute("wishlistCount", 0);
            return "mypage/wishlist";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Long uid = user.getUid();

        var wishlistRows = wishlistRepository.findByUser_UidOrderByCreatedAtDesc(uid);

        model.addAttribute("wishlistCount",
                wishlistRepository.countByUser_Uid(uid));

        model.addAttribute("wishlists", wishlistRows);

        return "mypage/wishlist";
    }
}