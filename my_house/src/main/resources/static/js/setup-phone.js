document.addEventListener('DOMContentLoaded', () => {
    const phoneInput = document.getElementById('phoneNumber');
    const nextBtn = document.getElementById('btnSendSms');
    
    // CSRF 토큰 정보 가져오기
    const csrfHeader = document.getElementById('csrfHeader').value;
    const csrfToken = document.getElementById('csrfToken').value;

    // 입력값에 따른 버튼 활성화 상태 변경
    phoneInput.addEventListener('input', () => {
        // 숫자만 남기기
        phoneInput.value = phoneInput.value.replace(/[^0-9]/g, '');
        
        if (phoneInput.value.length >= 10) {
            nextBtn.classList.add('active');
        } else {
            nextBtn.classList.remove('active');
        }
    });

    // 다음 버튼 클릭 이벤트
    nextBtn.addEventListener('click', () => {
        if (nextBtn.classList.contains('active')) {
            const phone = phoneInput.value;
            
            // ✅ 서버에 인증번호 발송 요청 (POST)
            fetch('/user/api/send-sms', { 
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader]: csrfToken
                },
                body: new URLSearchParams({ phone: phone })
            })
            .then(res => {
                if (res.ok) {
                    // 발송 성공 시 인증번호 입력 화면으로 이동 (쿼리 스트링으로 번호 전달)
                    location.href = `/user/verify-phone?phone=${phone}`;
                } else {
                    alert("인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
                }
            })
            .catch(err => {
                console.error("Error:", err);
                alert("서버 통신 오류가 발생했습니다.");
            });
        }
    });
});