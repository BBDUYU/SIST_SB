// /js/inquiry.js
document.addEventListener('DOMContentLoaded', () => {
  // 1) 작성날짜 표시(화면용)
  const createdDateEl = document.getElementById('createdDate');
  if (createdDateEl) {
    const now = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    createdDateEl.textContent = `${now.getFullYear()}.${pad(now.getMonth() + 1)}.${pad(now.getDate())}`;
  }

  // 2) 취소 버튼
  const cancelBtn = document.getElementById('cancelBtn');
  if (cancelBtn) {
    cancelBtn.addEventListener('click', (e) => {
      e.preventDefault();
      if (confirm('작성을 취소하시겠습니까?')) {
        history.back(); // 필요하면 location.href = '/mypage';
      }
    });
  }

  // 3) 폼 제출은 서버로 그대로 보냄 (절대 preventDefault 금지)
  const form = document.getElementById('inquiryForm');
  if (form) {
    form.addEventListener('submit', () => {
      // 선택: 중복 클릭 방지 (저장 버튼 비활성화)
      const submitBtn = form.querySelector('button[type="submit"]');
      if (submitBtn) submitBtn.disabled = true;
    });
  }
});