document.addEventListener('DOMContentLoaded', () => {
    const codeInput = document.getElementById('smsCode');
    const verifyBtn = document.getElementById('btnVerifySms');
    const resendBtn = document.getElementById('btnResendSms'); // ✅ 재전송 버튼 추가
    const urlParams = new URLSearchParams(window.location.search);
    const phone = urlParams.get('phone');
    
    const header = document.getElementById('csrfHeader').value;
    const token = document.getElementById('csrfToken').value;

    if (phone) {
        document.getElementById('displayPhone').innerText = phone;
    }

    // 1. 입력 로직 수정 (6자리 체크)
    codeInput.addEventListener('input', () => {
        codeInput.value = codeInput.value.replace(/[^0-9]/g, '');
        // ✅ 4에서 6으로 변경
        verifyBtn.className = codeInput.value.length === 6 ? 'btn-next active' : 'btn-next';
    });

    // 2. 재전송 로직 추가
    resendBtn.addEventListener('click', () => {
        fetch('/user/api/send-sms', { // ✅ 컨트롤러 @PostMapping("/api/send-sms") 호출
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [header]: token
            },
            body: new URLSearchParams({ phone: phone }) // @RequestParam 대응
        })
        .then(res => {
            if (res.ok) {
                alert("인증번호가 재발송되었습니다.");
                // 필요하다면 여기서 타이머 리셋 함수 호출
            } else {
                alert("재발송 실패. 잠시 후 다시 시도해주세요.");
            }
        })
        .catch(err => console.error("Error:", err));
    });

    // 3. 완료 버튼 클릭 로직 (기존 유지)
    verifyBtn.addEventListener('click', () => {
        if (verifyBtn.classList.contains('active')) {
            const code = codeInput.value;

            fetch('/user/api/verify-phone', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json', 
                    [header]: token
                },
                body: JSON.stringify({ 
                    phone: phone, 
                    code: code 
                })
            })
            .then(res => {
                if (res.ok) {
                    alert("인증이 완료되었습니다!");
                    location.href = "/user/login"; 
                } else {
                    alert("인증번호가 일치하지 않습니다.");
                }
            })
            .catch(err => console.error("Error:", err));
        }
    });
});