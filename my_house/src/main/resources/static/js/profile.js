// 회원정보 수정 페이지 JavaScript (profile.js)

const form = document.getElementById('profileForm');

const nicknameEl = document.getElementById('nickname');
const phoneEl = document.getElementById('phone');

const currentPasswordEl = document.getElementById('currentPassword');
const newPasswordEl = document.getElementById('newPassword');
const confirmPasswordEl = document.getElementById('confirmPassword');

// ✅ 휴대폰 번호 자동 하이픈
phoneEl?.addEventListener('input', (e) => {
  let v = e.target.value.replace(/\D/g, '').slice(0, 11); // 숫자만 11자리
  if (v.length >= 4 && v.length <= 7) {
    v = v.slice(0, 3) + '-' + v.slice(3);
  } else if (v.length >= 8) {
    v = v.slice(0, 3) + '-' + v.slice(3, 7) + '-' + v.slice(7);
  }
  e.target.value = v;
});

// ✅ 폼 제출 전 유효성 검사
form?.addEventListener('submit', function (e) {
  e.preventDefault();

  const nickname = nicknameEl?.value.trim() ?? '';
  const phone = phoneEl?.value.trim() ?? '';

  const currentPassword = currentPasswordEl?.value ?? '';
  const newPassword = newPasswordEl?.value ?? '';
  const confirmPassword = confirmPasswordEl?.value ?? '';

  // 닉네임 검증
  if (nickname === '') {
    alert('닉네임을 입력해주세요.');
    nicknameEl?.focus();
    return;
  }

  // (선택) 휴대폰 번호 검증: 입력했을 때만 검사
  // 010-1234-5678 형태(10~11자리) 허용
  if (phone !== '') {
    const phoneRegex = /^01[0-9]-\d{3,4}-\d{4}$/;
    if (!phoneRegex.test(phone)) {
      alert('휴대폰 번호 형식이 올바르지 않습니다. 예) 010-1234-5678');
      phoneEl?.focus();
      return;
    }
  }

  // 비밀번호 변경 검증 (비밀번호 필드가 입력된 경우에만)
  if (currentPassword || newPassword || confirmPassword) {
    if (!currentPassword) {
      alert('현재 비밀번호를 입력해주세요.');
      currentPasswordEl?.focus();
      return;
    }

    if (!newPassword) {
      alert('새 비밀번호를 입력해주세요.');
      newPasswordEl?.focus();
      return;
    }

    if (newPassword.length < 8) {
      alert('새 비밀번호는 8자 이상이어야 합니다.');
      newPasswordEl?.focus();
      return;
    }

    // 영문 + 숫자 + 특수문자 조합
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (!passwordRegex.test(newPassword)) {
      alert('새 비밀번호는 영문, 숫자, 특수문자를 조합하여 8자 이상이어야 합니다.');
      newPasswordEl?.focus();
      return;
    }

    if (newPassword !== confirmPassword) {
      alert('새 비밀번호가 일치하지 않습니다.');
      confirmPasswordEl?.focus();
      return;
    }
  }

  // 검증 통과 시 폼 제출
  this.submit();
});

// 취소 버튼 클릭 (HTML에서 onclick="handleCancel()"로 호출)
function handleCancel() {
  if (confirm('변경사항이 저장되지 않습니다. 취소하시겠습니까?')) {
    window.location.href = '/mypage';
  }
}

// 실시간 비밀번호 강도 체크(선택)
newPasswordEl?.addEventListener('input', function (e) {
  const password = e.target.value;

  if (password.length > 0 && password.length < 8) {
    this.style.borderColor = '#dc2626';
    return;
  }

  if (password.length >= 8) {
    const hasLetter = /[A-Za-z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSpecial = /[@$!%*#?&]/.test(password);

    if (hasLetter && hasNumber && hasSpecial) {
      this.style.borderColor = '#10b981';
    } else {
      this.style.borderColor = '#f59e0b';
    }
  } else {
    this.style.borderColor = '';
  }
});

// 비밀번호 확인 실시간 체크
confirmPasswordEl?.addEventListener('input', function (e) {
  const newPassword = newPasswordEl?.value ?? '';
  const confirmPassword = e.target.value;

  if (confirmPassword.length === 0) {
    this.style.borderColor = '';
    return;
  }

  if (newPassword === confirmPassword) {
    this.style.borderColor = '#10b981';
  } else {
    this.style.borderColor = '#dc2626';
  }
});