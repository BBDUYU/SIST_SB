package org.doit.ik.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createInquiry(String email, InquiryCreateRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자 없음: " + email));

        if (req.getTitle() == null || req.getTitle().isBlank())
            throw new IllegalArgumentException("제목을 입력해주세요.");
        if (req.getContent() == null || req.getContent().isBlank())
            throw new IllegalArgumentException("내용을 입력해주세요.");

        Inquiry inquiry = new Inquiry();
        inquiry.setUser(user);
        inquiry.setTitle(req.getTitle().trim());
        inquiry.setContent(req.getContent().trim());

        // 안전하게 기본값 명시
        inquiry.setStatus(InquiryStatus.WAITING);
        inquiry.setDeleted(false);

        Inquiry saved = inquiryRepository.saveAndFlush(inquiry);
        return saved.getInquiryId();
    }
    
    @Transactional
    public void answerInquiry(Long inquiryId, String answerContent) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다. ID: " + inquiryId));

        inquiry.setAnswer(answerContent);
        inquiry.setStatus(InquiryStatus.COMPLETED); // 답변 완료 상태로 변경
        inquiry.setAnsweredAt(LocalDateTime.now());
        
        // dirty checking으로 인해 save() 호출 생략 가능하지만 명시적으로 해도 무방
    }

    // 관리자 조회용 메서드 추가
    public List<Inquiry> getAllInquiriesForAdmin(String status, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return inquiryRepository.findByTitleContainingOrContentContainingOrUser_NicknameContainingAndDeletedFalse(keyword, keyword, keyword);
        }
        
        if ("UNANSWERED".equals(status)) {
            return inquiryRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(InquiryStatus.WAITING);
        } else if ("ANSWERED".equals(status)) {
            return inquiryRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(InquiryStatus.COMPLETED);
        }
        
        return inquiryRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }
    
    
}