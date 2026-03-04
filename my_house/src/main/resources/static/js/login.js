document.addEventListener('DOMContentLoaded', function() {
    // 1. URL에서 파라미터 추출
    const urlParams = new URLSearchParams(window.location.search);
    
    // 2. 'sent' 파라미터가 true인지 확인
    if (urlParams.get('sent') === 'true') {
        showPasswordMailToast();
    }
});

function showPasswordMailToast() {
    const msgBox = document.createElement('div');
    msgBox.className = 'success-toast'; // 기존 회원가입용 CSS 그대로 사용
    msgBox.innerHTML = `
        <div class="toast-content">
            <p>📧 임시 비밀번호 전송 완료!</p>
            <p class="sub">입력하신 이메일로 임시 비밀번호가 발송되었습니다.</p>
        </div>
    `;
    document.body.appendChild(msgBox);
    
    // 3초 후 자연스럽게 사라지게 설정 (선택 사항)
    setTimeout(() => {
        msgBox.style.opacity = '0';
        msgBox.style.transition = 'opacity 0.5s ease';
        setTimeout(() => msgBox.remove(), 500);
    }, 3000);
}