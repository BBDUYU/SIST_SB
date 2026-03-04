package org.doit.ik.user;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.doit.ik.complex.RecentlyViewed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final ProfileService profileService;
    private final InquiryService inquiryService;
    private final org.doit.ik.complex.ComplexRepository complexRepository;

    @GetMapping({"", "/"})
    public String mypage(Model model, Principal principal) {

        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);

        if (principal == null) {
            model.addAttribute("user", Map.of());
            model.addAttribute("stats", Map.of(
                    "recentView", 0,
                    "reviewCount", 0,
                    "inquiryCount", 0,
                    "wishlistCount", 0
            ));
            model.addAttribute("properties", List.of());
            model.addAttribute("reviewStats", Map.of("averageScore", 0.0));
            model.addAttribute("reviews", List.of());
            model.addAttribute("inquiries", List.of());
            return "/mypage/mypage";
        }

        String email = principal.getName();
        User loginUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 없음: " + email));

        Long uid = loginUser.getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("name", loginUser.getName());
        user.put("grade", loginUser.getRole());
        user.put("email", loginUser.getEmail());
        user.put("phone", loginUser.getPhone());
        user.put("address", "");
        user.put("nickname", loginUser.getNickname());
        model.addAttribute("user", user);

        long recentCnt = recentlyViewedRepository.countByUser_Uid(uid);
        long reviewCnt = reviewRepository.countByUser_UidAndDeletedAtIsNull(uid);
        long inquiryCnt = inquiryRepository.countByUser_UidAndDeletedFalse(uid);
        long wishlistCnt = wishlistRepository.countByUser_Uid(uid);

        Map<String, Object> stats = new HashMap<>();
        stats.put("recentView", recentCnt);
        stats.put("reviewCount", reviewCnt);
        stats.put("inquiryCount", inquiryCnt);
        stats.put("wishlistCount", wishlistCnt);
        model.addAttribute("stats", stats);

        List<RecentlyViewed> recentRows =
                recentlyViewedRepository.findTop4ByUser_UidOrderByViewedAtDesc(uid);

        List<Map<String, Object>> properties = new ArrayList<>();
        for (RecentlyViewed rv : recentRows) {
            Map<String, Object> p = new HashMap<>();
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

            // ✅ 추가: cid 내려주기
            Long cid = (r.getComplex() != null ? r.getComplex().getCid() : null);
            row.put("cid", cid);

            String fullName = null;
            if (r.getComplex() != null) {
                fullName = r.getComplex().getFullName();
            }

            if (fullName == null || fullName.isBlank()) {
                row.put("propertyName", "단지ID: " + (cid != null ? cid : ""));
            } else {
                row.put("propertyName", fullName);
            }

            row.put("rating", r.getRating());
            row.put("date", formatDate(r.getCreatedAt()));
            row.put("content", r.getContent());
            row.put("likes", 0);
            row.put("comments", 0);
            reviews.add(row);
        }
        model.addAttribute("reviews", reviews);

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

        if (principal == null) {
            model.addAttribute("user", Map.of());
            return "/mypage/profile";
        }

        String email = principal.getName();
        User loginUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 없음: " + email));

        Map<String, Object> user = new HashMap<>();
        user.put("nickname", loginUser.getNickname());
        user.put("phone", loginUser.getPhone());
        user.put("email", loginUser.getEmail());
        user.put("name", loginUser.getName());

        model.addAttribute("user", user);
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
            return "/mypage/wishlist";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Long uid = user.getUid();

        var wishlistRows = wishlistRepository.findByUser_UidOrderByCreatedAtDesc(uid);

        model.addAttribute("wishlistCount", wishlistRepository.countByUser_Uid(uid));

        List<Map<String, Object>> wishlists = new ArrayList<>();

        for (Wishlist w : wishlistRows) {
            Map<String, Object> row = new HashMap<>();

            row.put("wishlistId", w.getWishlistId());

            Long cid = (w.getComplex() != null ? w.getComplex().getCid() : null);
            
            row.put("cid", cid);
            
            row.put("lat", w.getComplex() != null ? w.getComplex().getLatitude() : null);
            row.put("lng", w.getComplex() != null ? w.getComplex().getLongitude() : null);

            String title = (w.getComplex() != null ? w.getComplex().getFullName() : null);
            row.put("title", (title != null && !title.isBlank()) ? title : ("단지ID: " + cid));

            row.put("location", "");
            row.put("imageUrl", "/images/sample/property1.jpg");
            row.put("type", "관심");
            row.put("price", "");
            row.put("detailUrl", "/main?cid=" + cid);

            wishlists.add(row);
        }

        model.addAttribute("wishlists", wishlists);

        return "/mypage/wishlist";
    }

    @PostMapping("/profile/update")
    public String updateProfile(ProfileUpdateRequest req,
                                Principal principal,
                                RedirectAttributes ra) {
        if (principal == null) return "redirect:/user/login";

        try {
            profileService.updateProfile(principal.getName(), req);
            ra.addFlashAttribute("msg", "저장되었습니다.");
            return "redirect:/mypage";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/profile";
        }
    }

    @PostMapping("/inquiry/create")
    public String createInquiry(InquiryCreateRequest req,
                                Principal principal,
                                RedirectAttributes ra) {
        if (principal == null) return "redirect:/user/login";

        try {
            Long id = inquiryService.createInquiry(principal.getName(), req);
            ra.addFlashAttribute("msg", "문의가 등록되었습니다. (ID=" + id + ")");
            return "redirect:/mypage";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/inquiry";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "문의 등록 실패: " + e.getMessage());
            return "redirect:/mypage/inquiry";
        }
    }

    // ✅ 하트 토글(위시리스트)
    @PostMapping("/wishlist/toggle")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> toggleWishlist(
            @org.springframework.web.bind.annotation.RequestParam("cid") Long cid,
            Principal principal) {

        if (principal == null) {
            return Map.of("ok", false, "reason", "UNAUTHORIZED");
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 없음: " + email));

        Long uid = user.getUid();

        boolean exists = wishlistRepository.existsByUser_UidAndComplex_Cid(uid, cid);

        if (exists) {
            wishlistRepository.findByUser_UidAndComplex_Cid(uid, cid)
                    .ifPresent(wishlistRepository::delete);
            return Map.of("ok", true, "hearted", false);
        } else {
            var complex = complexRepository.findById(cid)
                    .orElseThrow(() -> new IllegalStateException("단지 없음: " + cid));
            Wishlist w = new Wishlist();
            w.setUser(user);
            w.setComplex(complex);
            wishlistRepository.save(w);
            return Map.of("ok", true, "hearted", true);
        }
    }
    
    @GetMapping("/wishlist/ids")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<Long> wishlistIds(Principal principal) {
        if (principal == null) return List.of();

        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Long uid = user.getUid();

        return wishlistRepository.findCidListByUid(uid);
    }
    
    @PostMapping("/wishlist/remove")
    @ResponseBody
    public Map<String, Object> removeWishlist(@RequestParam("id") Long id) {
        wishlistRepository.deleteById(id);
        return Map.of("ok", true);
    }
}