package org.doit.ik.board;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BoardController {

    @GetMapping("/board")
    public String board(@RequestParam(name = "category", defaultValue = "free") String category,
                        Model model,
                        Principal principal) {

        model.addAttribute("isLogin", principal != null);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("posts", samplePosts());

        return "board/list";
    }

    @GetMapping("/board/{postId}")
    public String detail(@PathVariable("postId") Long postId,
                         Model model,
                         Principal principal) {

        model.addAttribute("isLogin", principal != null);

        Map<String, Object> post = samplePosts().stream()
            .filter(item -> postId.equals(item.get("id")))
            .findFirst()
            .orElse(Map.of(
                "id", postId,
                "category", "자유게시판",
                "title", "삭제되었거나 존재하지 않는 게시글입니다.",
                "author", "알 수 없음",
                "views", 0,
                "date", "-",
                "content", "요청하신 게시글을 찾을 수 없습니다."
            ));

        model.addAttribute("post", post);
        return "board/detail";
    }

    @GetMapping("/board/write")
    public String write(Model model, Principal principal) {
        model.addAttribute("isLogin", principal != null);
        model.addAttribute("formMode", "write");
        model.addAttribute("pageTitle", "새 글 쓰기");
        model.addAttribute("post", Map.of(
            "category", "free",
            "title", "",
            "content", ""
        ));
        return "board/form";
    }

    @GetMapping("/board/{postId}/edit")
    public String edit(@PathVariable("postId") Long postId,
                       Model model,
                       Principal principal) {

        model.addAttribute("isLogin", principal != null);
        model.addAttribute("formMode", "edit");
        model.addAttribute("pageTitle", "게시글 수정");

        Map<String, Object> selected = samplePosts().stream()
            .filter(item -> postId.equals(item.get("id")))
            .findFirst()
            .orElse(samplePosts().get(0));

        model.addAttribute("post", Map.of(
            "id", selected.get("id"),
            "category", toCategoryCode((String) selected.get("category")),
            "title", selected.get("title"),
            "content", selected.get("content")
        ));

        return "board/form";
    }

    private String toCategoryCode(String category) {
        return switch (category) {
            case "공지사항" -> "notice";
            case "Q&A" -> "qna";
            default -> "free";
        };
    }

    private List<Map<String, Object>> samplePosts() {
        return List.of(
            Map.of(
                "id", 1L,
                "category", "공지사항",
                "title", "[공지] 6월 정기 점검 안내",
                "author", "관리자",
                "views", 1203,
                "date", "2026-06-21",
                "content", "6월 30일 새벽 2시부터 4시까지 시스템 점검이 진행됩니다."
            ),
            Map.of(
                "id", 2L,
                "category", "자유게시판",
                "title", "신림역 근처 원룸 후기 공유해요",
                "author", "집구해요",
                "views", 248,
                "date", "2026-06-20",
                "content", "역에서 도보 7분 정도이고 밤길도 밝아서 괜찮았습니다."
            ),
            Map.of(
                "id", 3L,
                "category", "Q&A",
                "title", "전세자금대출 서류는 뭐가 필요한가요?",
                "author", "첫자취",
                "views", 83,
                "date", "2026-06-20",
                "content", "기본 재직증명서 외에 추가로 챙겨야 하는 서류가 있을까요?"
            ),
            Map.of(
                "id", 4L,
                "category", "자유게시판",
                "title", "안전 점수 높은 동네 추천 부탁드립니다",
                "author", "서울살이",
                "views", 176,
                "date", "2026-06-19",
                "content", "여성 1인 가구 기준으로 야간 귀가 안전한 곳 추천 부탁드려요."
            ),
            Map.of(
                "id", 5L,
                "category", "Q&A",
                "title", "관리비에 포함되는 항목 확인 방법",
                "author", "문의왕",
                "views", 91,
                "date", "2026-06-19",
                "content", "관리비 명세서에서 필수 확인 항목이 궁금합니다."
            ),
            Map.of(
                "id", 6L,
                "category", "자유게시판",
                "title", "이사 체크리스트 같이 만들어봐요",
                "author", "이사초보",
                "views", 144,
                "date", "2026-06-18",
                "content", "가전 이전, 주소 변경, 인터넷 이전 순서 팁 공유해요."
            )
        );
    }
}