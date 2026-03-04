package org.doit.ik.admin;

import java.util.List;

import org.doit.ik.user.Inquiry;
import org.doit.ik.user.InquiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/qna")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;
    

 // AdminInquiryController.java 수정

    @GetMapping
    public String qnaList(
        @RequestParam(value = "status", required = false) String status, // value 추가
        @RequestParam(value = "q", required = false) String keyword,     // name="q" 또는 value="q"
        Model model) {
        
        List<Inquiry> questions = inquiryService.getAllInquiriesForAdmin(status, keyword);
        model.addAttribute("questions", questions);
        return "admin/qna";
    }

    @PostMapping("/answer")
    @ResponseBody
    public ResponseEntity<?> submitAnswer(
        @RequestParam(value = "id") Long id,           // value 추가
        @RequestParam(value = "content") String content // value 추가
    ) {
        try {
            inquiryService.answerInquiry(id, content);
            return ResponseEntity.ok("답변이 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
