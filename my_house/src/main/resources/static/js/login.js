document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);

    // 1. 차단된 사용자 등 세션 에러 메시지 처리
    const serverErrorMsg = window.LOGIN_ERROR_MSG;
    if (serverErrorMsg) {
        alert(serverErrorMsg); // "관리자에 의해 정지된 계정입니다." 출력
    } 
    // 2. 일반 로그인 실패 처리 (이메일/비번 불일치)
    else if (urlParams.get('error') === 'true') {
        alert("로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.");
    }
});

// 토스트 함수는 그대로 유지
function showPasswordMailToast() {
    const msgBox = document.createElement('div');
    msgBox.className = 'success-toast';
    msgBox.innerHTML = `
        <div class="toast-content">
            <p>📧 임시 비밀번호 전송 완료!</p>
            <p class="sub">입력하신 이메일로 임시 비밀번호가 발송되었습니다.</p>
        </div>
    `;
    document.body.appendChild(msgBox);
    
    setTimeout(() => {
        msgBox.style.opacity = '0';
        msgBox.style.transition = 'opacity 0.5s ease';
        setTimeout(() => msgBox.remove(), 500);
    }, 3000);
}

