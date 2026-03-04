package org.doit.ik.admin;

import org.doit.ik.user.Review;
import org.doit.ik.user.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewRepository reviewRepository;

    @GetMapping
    public String list(
        @RequestParam(name="status", defaultValue="ACTIVE") Review.ReviewStatus status,
        @RequestParam(name="q", required=false) String q,
        @RequestParam(name="page", defaultValue="0") int page,
        Model model
    ) {
        PageRequest pageable = PageRequest.of(page, 10);

        Page<Review> reviews = (q == null || q.isBlank())
                ? reviewRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : reviewRepository.searchByStatus(status, q.trim(), pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("status", status);
        model.addAttribute("q", q);

        return "admin/reviews";
    }

    @PostMapping("/{id}/approve")
    public String approve(
        @PathVariable("id") Long id,
        @RequestParam(name="status", defaultValue="ACTIVE") Review.ReviewStatus status,
        @RequestParam(name="q", required=false) String q,
        @RequestParam(name="page", defaultValue="0") int page) {

        Review r = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음: " + id));

        r.setStatus(Review.ReviewStatus.ACTIVE);
        reviewRepository.save(r);

        return redirect(status, q, page);
    }

    @PostMapping("/{id}/hide")
    public String hide(
        @PathVariable("id") Long id,
        @RequestParam(name="status", defaultValue="ACTIVE") Review.ReviewStatus status,
        @RequestParam(name="q", required=false) String q,
        @RequestParam(name="page", defaultValue="0") int page) {

        Review r = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음: " + id));

        r.setStatus(Review.ReviewStatus.INACTIVE);
        reviewRepository.save(r);

        return redirect(status, q, page);
    }

    private String redirect(Review.ReviewStatus status, String q, int page) {
        StringBuilder sb = new StringBuilder("redirect:/admin/reviews?status=")
                .append(status.name()).append("&page=").append(page);
        if (q != null && !q.isBlank()) sb.append("&q=").append(q);
        return sb.toString();
    }
}