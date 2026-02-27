// 회원정보 수정 페이지 JavaScript

// 폼 제출 전 유효성 검사
document.getElementById('profileForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    const nickname = document.getElementById('nickname').value.trim();
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    
    // 닉네임 검증
    if (nickname === '') {
        alert('닉네임을 입력해주세요.');
        document.getElementById('nickname').focus();
        return false;
    }
    
    // 비밀번호 변경 검증 (비밀번호 필드가 입력된 경우에만)
    if (currentPassword || newPassword || confirmPassword) {
        // 현재 비밀번호 확인
        if (!currentPassword) {
            alert('현재 비밀번호를 입력해주세요.');
            document.getElementById('currentPassword').focus();
            return false;
        }
        
        // 새 비밀번호 확인
        if (!newPassword) {
            alert('새 비밀번호를 입력해주세요.');
            document.getElementById('newPassword').focus();
            return false;
        }
        
        // 새 비밀번호 길이 확인
        if (newPassword.length < 8) {
            alert('새 비밀번호는 8자 이상이어야 합니다.');
            document.getElementById('newPassword').focus();
            return false;
        }
        
        // 새 비밀번호 복잡도 확인 (영문, 숫자, 특수문자 조합)
        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
        if (!passwordRegex.test(newPassword)) {
            alert('새 비밀번호는 영문, 숫자, 특수문자를 조합하여 8자 이상이어야 합니다.');
            document.getElementById('newPassword').focus();
            return false;
        }
        
        // 비밀번호 확인 일치 여부
        if (newPassword !== confirmPassword) {
            alert('새 비밀번호가 일치하지 않습니다.');
            document.getElementById('confirmPassword').focus();
            return false;
        }
    }
    
    // 검증 통과 시 폼 제출
    this.submit();
});

// 취소 버튼 클릭
function handleCancel() {
    if (confirm('변경사항이 저장되지 않습니다. 취소하시겠습니까?')) {
        // 이전 페이지로 이동 또는 마이페이지로 이동
        window.location.href = '/mypage';
    }
}

// 실시간 비밀번호 강도 체크 (선택사항)
document.getElementById('newPassword')?.addEventListener('input', function(e) {
    const password = e.target.value;
    
    // 비밀번호 강도 표시 로직 (필요시 추가)
    if (password.length > 0 && password.length < 8) {
        this.style.borderColor = '#dc2626';
    } else if (password.length >= 8) {
        const hasLetter = /[A-Za-z]/.test(password);
        const hasNumber = /\d/.test(password);
        const hasSpecial = /[@$!%*#?&]/.test(password);
        
        if (hasLetter && hasNumber && hasSpecial) {
            this.style.borderColor = '#10b981';
        } else {
            this.style.borderColor = '#f59e0b';
        }
    }
});

// 비밀번호 확인 실시간 체크
document.getElementById('confirmPassword')?.addEventListener('input', function(e) {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = e.target.value;
    
    if (confirmPassword.length > 0) {
        if (newPassword === confirmPassword) {
            this.style.borderColor = '#10b981';
        } else {
            this.style.borderColor = '#dc2626';
        }
    }
});