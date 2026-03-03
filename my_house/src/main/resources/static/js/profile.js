// 회원정보 수정 페이지 JavaScript (profile.js)

const form = document.getElementById('profileForm');

const nicknameEl = document.getElementById('nickname');
const phoneEl = document.getElementById('phone');

const currentPasswordEl = document.getElementById('currentPassword');
const newPasswordEl = document.getElementById('newPassword');
const confirmPasswordEl = document.getElementById('confirmPassword');

// ✅ 휴대폰 번호: 하이픈 없이 "숫자만" 입력되게
phoneEl?.addEventListener('input', (e) => {
  e.target.value = e.target.value.replace(/\D/g, '').slice(0, 11); // 숫자만, 최대 11자리
});

// ✅ 폼 제출 전 유효성 검사
form?.addEventListener('submit', function (e) {
  e.preventDefault();

  const nickname = nicknameEl?.value.trim() ?? '';
  const phone = phoneEl?.value.trim() ?? ''; // 이미 숫자만 들어있게 됨

  const currentPassword = currentPasswordEl?.value ?? '';
  const newPassword = newPasswordEl?.value ?? '';
  const confirmPassword = confirmPasswordEl?.value ?? '';

  // 닉네임 검증
  if (nickname === '') {
    alert('닉네임을 입력해주세요.');
    nicknameEl?.focus();
    return;
  }

  // ✅ 휴대폰 번호 검증(하이픈 없이)
  if (phone !== '') {
    const phoneRegex = /^01[0-9]\d{7,8}$/; // 10~11자리: 01012345678

    if (!phoneRegex.test(phone)) {
      alert('휴대폰 번호 형식이 올바르지 않습니다. 예) 01012345678');
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

  this.submit();
});

// 취소 버튼 (필요시)
function handleCancel() {
  if (confirm('변경사항이 저장되지 않습니다. 취소하시겠습니까?')) {
    window.location.href = '/mypage';
  }
}

// 브라우저 뒤로가기(←)를 /mypage로 강제
(function forceBackToMypage() {
  const TARGET = '/mypage';

  // 1) 히스토리가 없을 수도 있으니 "가짜 한 칸"을 만들어 뒤로가기가 눌리게 함
  // (직접 URL 입력으로 들어온 경우에도 동작)
  history.pushState({ guard: true }, '', location.href);

  // 2) 뒤로 버튼(popstate) 발생 시 /mypage로 이동
  window.addEventListener('popstate', function () {
    location.href = TARGET;
  });
})();