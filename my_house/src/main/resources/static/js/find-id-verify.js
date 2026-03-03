// static/js/find-id-verify.js

document.addEventListener('DOMContentLoaded', function() {
	console.log("JS 로드 완료");
    let timeLeft = 180; 
    const timerElement = document.getElementById('timer');
    const resendBtn = document.getElementById('resendBtn');
    const submitBtn = document.getElementById('submitBtn');

    // HTML에 숨겨둔 데이터 가져오기
    const phone = document.getElementById('phoneData').value;
    const csrfToken = document.getElementById('csrfToken').value;
    const csrfHeader = document.getElementById('csrfHeader').value;

    // 1. 타이머 구동 함수
    function startTimer() {
        const countdown = setInterval(() => {
            if (timeLeft <= 0) {
                clearInterval(countdown);
                timerElement.innerText = "만료";
                submitBtn.disabled = true;
                alert("인증 시간이 만료되었습니다. 다시 시도해주세요.");
            } else {
                timeLeft--;
                const minutes = Math.floor(timeLeft / 60);
                const seconds = timeLeft % 60;
                timerElement.innerText = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
            }
        }, 1000);
    }

    startTimer();

    // 2. 재전송 버튼 클릭 이벤트
    resendBtn.onclick = function() {
        fetch('/api/find-id/send', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                // ✅ 중요: Spring Security POST 요청을 위해 CSRF 헤더 반드시 포함
                [csrfHeader]: csrfToken 
            },
            body: JSON.stringify({ phone: phone })
        })
        .then(response => {
            if (response.ok) {
                alert("인증번호가 재발송되었습니다.");
                timeLeft = 180; // 타이머 초기화
                submitBtn.disabled = false;
            } else {
                alert("1분 이내에는 재전송할 수 없습니다.");
            }
        });
    };
});