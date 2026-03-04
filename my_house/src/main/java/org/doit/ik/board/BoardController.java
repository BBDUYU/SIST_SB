package org.doit.ik.board;

import java.security.Principal;

import org.doit.ik.user.CustomUserDetails;
import org.doit.ik.user.User;
import org.doit.ik.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;

    @GetMapping("/board")
    public String board(
            @RequestParam(name = "category", defaultValue = "all") String category,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model,
            Principal principal
    ) {
        Page<Board> posts = boardService.list(category, q, PageRequest.of(page, 10));

        model.addAttribute("posts", posts);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("q", q);
        model.addAttribute("isLogin", principal != null);

        return "board/list";
    }

    @GetMapping("/board/{postId}")
    public String detail(@PathVariable("postId") Long postId,
                         Model model,
                         Principal principal) {

        model.addAttribute("isLogin", principal != null);

        Board post = boardService.get(postId);
        model.addAttribute("post", post);
        model.addAttribute("replies", boardService.getReplies(postId));
        
        User me = currentUser(principal);
        model.addAttribute("me", me);
        model.addAttribute("isAdmin", isAdmin(me));

        return "board/detail";
    }

    @GetMapping("/board/write")
    public String write(Model model, Principal principal) {

        model.addAttribute("isLogin", principal != null);

        User me = currentUser(principal); // principal.getName()으로 email 조회하는 너 메서드
        boolean isAdmin = (me != null && "ROLE_ADMIN".equals(me.getRole()));
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("formMode", "write");
        model.addAttribute("pageTitle", "새 글 쓰기");
        model.addAttribute("dto", new BoardDTO());

        return "board/form";
    }

    @PostMapping("/board/write")
    public String writeSubmit(@ModelAttribute("dto") BoardDTO dto, Principal principal) {
        if (principal == null) {
            return "redirect:/oauth2/authorization/kakao"; 
        }

        User writer = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + principal.getName()));

        Long id = boardService.create(dto, writer);
        return "redirect:/board/" + id;
    }

    @GetMapping("/board/{postId}/edit")
    public String edit(@PathVariable("postId") Long postId,
                       Model model,
                       Principal principal) {

        model.addAttribute("isLogin", principal != null);
        model.addAttribute("formMode", "edit");
        model.addAttribute("pageTitle", "게시글 수정");

        Board b = boardService.get(postId);

        BoardDTO dto = new BoardDTO();
        dto.setTitle(b.getTitle());
        dto.setContent(b.getContent());
        dto.setCategory(toCategoryCode(b.getSubCategory().getScName()));

        model.addAttribute("dto", dto);
        model.addAttribute("postId", postId);

        return "board/form";
    }

    @PostMapping("/board/{postId}/edit")
    public String editSubmit(@PathVariable("postId") Long postId,
                             @ModelAttribute("dto") BoardDTO dto,
                             Principal principal) {
        User me = currentUser(principal);
        boardService.update(postId, dto, me);
        return "redirect:/board/" + postId;
    }

    private String toCategoryCode(String name) {
        return switch (name) {
            case "공지사항" -> "notice";
            case "회원질문" -> "qna";
            default -> "free";
        };
    }
    
    @PostMapping("/board/{postId}/reply/{replyId}/delete")
    public String deleteReply(@PathVariable("postId") Long postId,
                              @PathVariable("replyId") Long replyId,
                              Principal principal) {

        User me = currentUser(principal);
        if (me == null) return "redirect:/oauth2/authorization/kakao";

        boardService.deleteReply(postId, replyId, me);
        return "redirect:/board/" + postId;
    }

    // 댓글
    @PostMapping("/board/{postId}/reply")
    public String addReply(@PathVariable("postId") Long postId,
                           @RequestParam("content") String content,
                           Principal principal) {

        User me = currentUser(principal);
        if (me == null) {
            return "redirect:/oauth2/authorization/kakao";
        }

        boardService.addReply(postId, content, me);
        return "redirect:/board/" + postId;
    }
    
    // 유저
    private User currentUser(Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Object p = auth.getPrincipal();

        // 1) 일반 로그인(CustomUserDetails 등) → username(email)로 조회
        if (p instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            String email = ud.getUsername();
            return userRepository.findByEmail(email).orElse(null);
        }

        // 2) OAuth2 로그인(카카오 등) → attributes에서 email 꺼내기
        if (p instanceof OAuth2User ou) {
            Object emailObj = ou.getAttribute("email"); // 구글은 보통 여기 있음
            if (emailObj instanceof String email && !email.isBlank()) {
                return userRepository.findByEmail(email).orElse(null);
            }

            // 카카오는 보통 kakao_account.email
            Object ka = ou.getAttribute("kakao_account");
            if (ka instanceof Map<?, ?> kakaoAccount) {
                Object kEmail = kakaoAccount.get("email");
                if (kEmail instanceof String email && !email.isBlank()) {
                    return userRepository.findByEmail(email).orElse(null);
                }
            }

            // 마지막 fallback: name이 email일 수도 있으니 한 번 더
            String name = auth.getName();
            return userRepository.findByEmail(name).orElse(null);
        }

        // 3) 기타 fallback
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private boolean isAdmin(User u) {
        return u != null && "ROLE_ADMIN".equalsIgnoreCase(u.getRole());
    }
    
    // 삭제
    @PostMapping("/board/{postId}/delete")
    public String deletePost(@PathVariable("postId") Long postId, Principal principal) {

        User me = currentUser(principal); // 너가 만든 currentUser 메서드
        boardService.delete(postId, me);

        return "redirect:/board";
    }
}