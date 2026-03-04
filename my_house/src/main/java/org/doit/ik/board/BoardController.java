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

        return "board/detail";
    }

    @GetMapping("/board/write")
    public String write(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("formMode", "write");
        model.addAttribute("pageTitle", "새 글 쓰기");
        model.addAttribute("dto", new BoardDTO());
        return "board/form";
    }

    @PostMapping("/board/write")
    public String writeSubmit(@ModelAttribute("dto") BoardDTO dto, Principal principal) {
        if (principal == null) {
            return "redirect:/oauth2/authorization/kakao"; // 너네 카카오 로그인 경로
        }

        // principal.getName() 이 로그에서 "user where email=?" 로 찍히는 거 보면
        // 여기 값이 email로 들어오고 있는 상태임.
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
                             @ModelAttribute("dto") BoardDTO dto) {

        boardService.update(postId, dto);
        return "redirect:/board/" + postId;
    }

    private String toCategoryCode(String name) {
        return switch (name) {
            case "공지사항" -> "notice";
            case "Q&A" -> "qna";
            default -> "free";
        };
    }

    // 댓글
    @PostMapping("/board/{postId}/reply")
    public String addReply(@PathVariable("postId") Long postId,
                           @RequestParam("content") String content,
                           Principal principal) {

        if (principal == null) {
            return "redirect:/oauth2/authorization/kakao";
        }

        User writer = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + principal.getName()));

        boardService.addReply(postId, content, writer);
        return "redirect:/board/" + postId;
    }
}