document.addEventListener('DOMContentLoaded', () => {
    const codeInput = document.getElementById('smsCode');
    const verifyBtn = document.getElementById('btnVerifySms');
    const urlParams = new URLSearchParams(window.location.search);
    const phone = urlParams.get('phone');

    if (phone) {
        document.getElementById('displayPhone').innerText = phone;
    }

    codeInput.addEventListener('input', () => {
        codeInput.value = codeInput.value.replace(/[^0-9]/g, '');
        // 4자리 입력 시 버튼 활성화
        verifyBtn.className = codeInput.value.length === 4 ? 'btn-next active' : 'btn-next';
    });

	verifyBtn.addEventListener('click', () => {
	    if (verifyBtn.classList.contains('active')) {
	        const code = codeInput.value;
	        const header = document.getElementById('csrfHeader').value;
	        const token = document.getElementById('csrfToken').value;

	        fetch('/user/api/verify-phone', {
	            method: 'POST',
	            headers: {
	                // 1. Content-Type을 JSON으로 명시하여 415 에러를 해결합니다.
	                'Content-Type': 'application/json', 
	                [header]: token
	            },
	            // 2. 데이터를 JSON 문자열로 변환하여 보냅니다.
	            body: JSON.stringify({ 
	                phone: phone, 
	                code: code 
	            })
	        })
	        .then(res => {
	            if (res.ok) {
	                alert("인증이 완료되었습니다!");
	                // 3. 메인 대신 로그인 페이지로 이동하도록 수정합니다.
	                location.href = "/user/login"; 
	            } else {
	                alert("인증번호가 일치하지 않습니다. 다시 확인해주세요.");
	            }
	        })
	        .catch(err => console.error("Error:", err));
	    }
	});
});