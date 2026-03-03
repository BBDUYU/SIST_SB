(() => {
    const $ = (sel, root = document) => root.querySelector(sel);

    // [요소 선택]
    const sendCodeBtn = $('#sendCodeBtn');
    const verificationArea = $('#verificationArea');
    const timerElement = $('#timer');
    const verifyCodeBtn = $('#verifyCodeBtn');
    const resendBtn = $('#resendBtn');
    const submitBtn = $('#submitBtn'); // 회원가입 제출 버튼
    const emailInput = $('#email');
    const authCodeInput = $('#authCode');
    const signupForm = $('#signupForm');

    let timeLeft = 180;
    let timerId = null;

    // [공통 헤더]
    const getHeaders = () => ({
        'Content-Type': 'application/json',
        [$('#csrfHeader').value]: $('#csrfToken').value
    });

    // ==========================================
    // 1. 이메일 인증 로직
    // ==========================================
    if (sendCodeBtn && verificationArea) {
        const startTimer = () => {
            if (timerId) clearInterval(timerId);
            timeLeft = 180;
            timerId = setInterval(() => {
                if (timeLeft <= 0) {
                    clearInterval(timerId);
                    timerElement.innerText = "만료";
                    verifyCodeBtn.disabled = true;
                    alert("인증 시간이 만료되었습니다. 다시 시도해주세요.");
                } else {
                    timeLeft--;
                    const min = Math.floor(timeLeft / 60);
                    const sec = timeLeft % 60;
                    timerElement.innerText = `${String(min).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
                }
            }, 1000);
        };

        const sendEmailCode = (isResend = false) => {
            const email = emailInput.value;
            if (!email || !email.includes('@')) {
                alert("올바른 이메일 주소를 입력해주세요.");
                return;
            }
            if (!isResend) {
                sendCodeBtn.disabled = true;
                sendCodeBtn.innerText = "발송중...";
            }

            fetch('/api/auth/email-send', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ email: email })
            })
            .then(res => {
                if (res.ok) {
                    alert(isResend ? "인증번호가 재발송되었습니다." : "인증번호가 발송되었습니다.");
                    verificationArea.style.display = 'block';
                    startTimer();
                    sendCodeBtn.innerText = "재발송";
                    verifyCodeBtn.disabled = false;
                } else {
                    alert("발송 실패. 다시 시도해주세요.");
                    if (!isResend) sendCodeBtn.innerText = "인증하기";
                }
                sendCodeBtn.disabled = false;
            });
        };

        sendCodeBtn.addEventListener('click', () => sendEmailCode(false));
        if (resendBtn) {
            resendBtn.addEventListener('click', (e) => {
                e.preventDefault();
                sendEmailCode(true);
            });
        }

        verifyCodeBtn.addEventListener('click', () => {
            const code = authCodeInput.value;
            if (code.length !== 6) {
                alert("인증번호 6자리를 정확히 입력해주세요.");
                return;
            }
            fetch('/api/auth/email-verify', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ email: emailInput.value, code: code })
            })
            .then(res => {
                if (res.ok) {
                    alert("인증에 성공했습니다.");
                    clearInterval(timerId);
                    timerElement.style.color = "#52c41a";
                    timerElement.innerText = "완료";
                    verifyCodeBtn.disabled = true;
                    emailInput.readOnly = true;
                    submitBtn.disabled = false; 
                } else {
                    alert("인증번호가 일치하지 않습니다.");
                    authCodeInput.value = "";
                    authCodeInput.focus();
                }
            });
        });
    }

    // ==========================================
    // 2. 약관 동의 로직
    // ==========================================
    const checkAll = $('#checkAll');
    const agreeTerms = $('input[name="agreeTerms"]');
    const agreePrivacy = $('input[name="agreePrivacy"]');
    const agreeMarketing = $('input[name="agreeMarketing"]');

    if (checkAll && agreeTerms && agreePrivacy && agreeMarketing) {
        const items = [agreeTerms, agreePrivacy, agreeMarketing];
        const syncCheckAll = () => {
            const allChecked = items.every(i => i.checked);
            const someChecked = items.some(i => i.checked);
            checkAll.checked = allChecked;
            checkAll.indeterminate = someChecked && !allChecked;
        };
        checkAll.addEventListener('change', (e) => {
            const isChecked = e.target.checked;
            items.forEach(i => (i.checked = isChecked));
            checkAll.indeterminate = false;
        });
        items.forEach(i => i.addEventListener('change', syncCheckAll));
        syncCheckAll();
    }

    // ==========================================
    // 3. 회원가입 제출 로직 (수정 완료)
    // ==========================================
    if (signupForm) {
        signupForm.addEventListener('submit', (e) => {
            e.preventDefault();

            const formData = new FormData(signupForm);
            const data = Object.fromEntries(formData.entries());

            // 체크박스 boolean 값 보정 (name 속성 기준)
            data.agreeTerms = $('input[name="agreeTerms"]').checked;
            data.agreePrivacy = $('input[name="agreePrivacy"]').checked;
            data.agreeMarketing = $('input[name="agreeMarketing"]').checked;

            fetch(signupForm.action, {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify(data)
            })
            .then(async res => {
                if (res.ok) {
                    showSuccessAndRedirect();
                } else {
                    const errorMsg = await res.text();
                    alert(errorMsg || "회원가입 중 오류가 발생했습니다.");
                }
            })
            .catch(err => console.error("Error:", err));
        });
    }
	function showSuccessAndRedirect() {
	    const msgBox = document.createElement('div');
	    msgBox.className = 'success-toast';
	    msgBox.innerHTML = `
	        <div class="toast-content">
	            <p>🎉 회원가입이 완료되었습니다!</p>
	            <p class="sub">잠시 후 휴대폰 번호 등록 페이지로 이동합니다.</p>
	        </div>
	    `;
	    document.body.appendChild(msgBox);

	    setTimeout(() => {
	        // ✅ 로그인 페이지가 아닌 번호 등록 페이지로 리다이렉트
	        // 컨트롤러에서 자동 로그인을 처리했으므로 바로 접근 가능합니다.
	        location.replace("/user/setup-phone"); 
	    }, 3000);
	}
})();