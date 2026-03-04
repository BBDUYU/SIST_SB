package org.doit.ik.user;

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
}