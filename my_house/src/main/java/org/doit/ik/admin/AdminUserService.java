package org.doit.ik.admin;

import java.util.List;
import org.doit.ik.user.UserRepository;
import org.doit.ik.user.UserStatus;
import org.doit.ik.user.User; // 엔티티 클래스
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<User> getAllMembers() {
        return userRepository.findAll(); 
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        user.setStatus(newStatus); // 상태 변경
        
        // 탈퇴 상태로 변경될 경우 탈퇴 시간 기록 (선택 사항)
        if (newStatus == UserStatus.DELETED) {
            user.setDeletedAt(java.time.LocalDateTime.now());
        } else {
            user.setDeletedAt(null); // 다시 복구될 경우 시간 초기화
        }
    }
    @Transactional
    public void updateUserRole(Long userId, String newRole) { // ✅ String 타입으로 수정
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setRole(newRole);
    }
}