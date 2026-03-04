package org.doit.ik.user;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import org.doit.ik.complex.RecentlyViewed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    private void setEmptyMyPageModel(Model model) {
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
    }

    @GetMapping({"", "/"})
    public String mypage(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);

        User loginUser = currentUser(principal);
        if (loginUser == null) {
            setEmptyMyPageModel(model);
            return "/mypage/mypage";
        }

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

        model.addAttribute("reviewStats", Map.of("averageScore", avgScore));

        List<Review> reviewRows =
                reviewRepository.findByUser_UidAndDeletedAtIsNullOrderByCreatedAtDesc(uid);

        List<Map<String, Object>> reviews = new ArrayList<>();
        for (Review r : reviewRows) {
            Map<String, Object> row = new HashMap<>();

            Long cid = (r.getComplex() != null ? r.getComplex().getCid() : null);
            row.put("cid", cid);

            String fullName = (r.getComplex() != null ? r.getComplex().getFullName() : null);
            row.put("propertyName",
                    (fullName == null || fullName.isBlank())
                            ? ("단지ID: " + (cid != null ? cid : ""))
                            : fullName);

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

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("alarmCount", 3);

        User loginUser = currentUser(principal);
        if (loginUser == null) {
            model.addAttribute("user", Map.of());
            return "/mypage/profile";
        }

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

        User user = currentUser(principal);
        if (user == null) {
            model.addAttribute("wishlists", List.of());
            model.addAttribute("wishlistCount", 0);
            return "/mypage/wishlist";
        }

        Long uid = user.getUid();
        var wishlistRows = wishlistRepository.findByUser_UidOrderByCreatedAtDesc(uid);

        model.addAttribute("wishlistCount", wishlistRepository.countByUser_Uid(uid));

        List<Map<String, Object>> wishlists = new ArrayList<>();
        for (Wishlist w : wishlistRows) {
            Map<String, Object> row = new HashMap<>();

            row.put("wishlistId", w.getWishlistId());

            Long cid = (w.getComplex() != null ? w.getComplex().getCid() : null);
            row.put("cid", cid);

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
    public String updateProfile(ProfileUpdateRequest req, Principal principal, RedirectAttributes ra) {
        User user = currentUser(principal);
        if (user == null) return "redirect:/user/login";

        try {
            profileService.updateProfile(user.getEmail(), req);
            ra.addFlashAttribute("msg", "저장되었습니다.");
            return "redirect:/mypage";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/profile";
        }
    }

    @PostMapping("/inquiry/create")
    public String createInquiry(InquiryCreateRequest req, Principal principal, RedirectAttributes ra) {
        User user = currentUser(principal);
        if (user == null) return "redirect:/user/login";

        try {
            Long id = inquiryService.createInquiry(user.getEmail(), req);
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

    @PostMapping("/wishlist/toggle")
    @ResponseBody
    public Map<String, Object> toggleWishlist(@RequestParam("cid") Long cid, Principal principal) {
        User user = currentUser(principal);
        if (user == null) return Map.of("ok", false, "reason", "UNAUTHORIZED");

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
    @ResponseBody
    public List<Long> wishlistIds(Principal principal) {
        User user = currentUser(principal);
        if (user == null) return List.of();
        return wishlistRepository.findCidListByUid(user.getUid());
    }

    @PostMapping("/wishlist/remove")
    @ResponseBody
    public Map<String, Object> removeWishlist(@RequestParam("id") Long id) {
        wishlistRepository.deleteById(id);
        return Map.of("ok", true);
    }

    private User currentUser(Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Object p = auth.getPrincipal();

        // 1) 일반 로그인 → email
        if (p instanceof UserDetails ud) {
            String email = ud.getUsername();
            return userRepository.findByEmail(email).orElse(null);
        }

        // 2) OAuth2 로그인(카카오 등)
        if (p instanceof OAuth2User ou) {
            // (A) email 우선
            Object emailObj = ou.getAttribute("email");
            if (emailObj instanceof String email && !email.isBlank()) {
                return userRepository.findByEmail(email).orElse(null);
            }

            Object ka = ou.getAttribute("kakao_account");
            if (ka instanceof Map<?, ?> kakaoAccount) {
                Object kEmail = kakaoAccount.get("email");
                if (kEmail instanceof String email && !email.isBlank()) {
                    return userRepository.findByEmail(email).orElse(null);
                }
            }

            if (auth instanceof OAuth2AuthenticationToken oat) {
                Provider provider = Provider.valueOf(oat.getAuthorizedClientRegistrationId().toUpperCase()); // KAKAO
                Object idObj = ou.getAttribute("id"); // kakao
                if (idObj != null) {
                    String providerId = String.valueOf(idObj);
                    return userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);
                }
            }

            return userRepository.findByEmail(auth.getName()).orElse(null);
        }

        return null;
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
}