// mypage.js (더미 제거 + 리뷰 바 0% 색 제거)

document.addEventListener('DOMContentLoaded', function () {
  const tabButtons = document.querySelectorAll('.tab-button');
  const tabContents = document.querySelectorAll('.tab-content');
  const tabIndicator = document.querySelector('.tab-indicator');

  // 초기 인디케이터
  updateIndicator(getActiveTabIndex());

  // 탭 클릭
  tabButtons.forEach((button, index) => {
    button.addEventListener('click', function () {
      const tabName = this.getAttribute('data-tab');
      if (!tabName) return;

      tabButtons.forEach(btn => btn.classList.remove('active'));
      tabContents.forEach(content => content.classList.remove('active'));

      this.classList.add('active');
      const target = document.getElementById(tabName + '-tab');
      if (target) target.classList.add('active');

      updateIndicator(index);
    });
  });

  // ✅ 통계(활동통계) 클릭 시 해당 탭으로 이동
  document.querySelectorAll('[data-go-tab]').forEach(el => {
    el.addEventListener('click', () => {
      const tabKey = el.getAttribute('data-go-tab');
      if (!tabKey) return;

      const targetBtn = document.querySelector(`.tab-button[data-tab="${tabKey}"]`);
      if (targetBtn) targetBtn.click();
    });
  });

  // ✅ 리뷰 막대(퍼센트) 초기화: 0%면 색 제거(.has-value 제거)
  initReviewBars();

  function getActiveTabIndex() {
    const active = document.querySelector('.tab-button.active');
    if (!active) return 0;
    return Array.from(tabButtons).indexOf(active);
  }

  function updateIndicator(index) {
    if (!tabIndicator || tabButtons.length === 0) return;
    const buttonWidth = 100 / tabButtons.length;
    tabIndicator.style.width = buttonWidth + '%';
    tabIndicator.style.left = (index * buttonWidth) + '%';
  }

  function initReviewBars() {
    for (let i = 1; i <= 5; i++) {
      const bar = document.getElementById('bar' + i);
      const pct = document.getElementById('percent' + i);
      if (!bar || !pct) continue;

      // 퍼센트 텍스트(예: "0%")에서 숫자만 추출
      const v = Number(String(pct.textContent).replace('%', '').trim()) || 0;

      // width 반영 + 0이면 색 제거
      bar.style.width = v + '%';
      bar.classList.toggle('has-value', v > 0);
    }
  }
});

/* -------------------------
   공통 클릭 이벤트(이벤트 위임)
------------------------- */
document.addEventListener('click', function (e) {
  // 하트 토글
  const heartBtn = e.target.closest('.btn-icon');
  if (heartBtn) {
    const svg = heartBtn.querySelector('svg');
    if (svg) {
      svg.setAttribute('fill', svg.getAttribute('fill') === 'currentColor' ? 'none' : 'currentColor');
    }
    return;
  }

  // 새 문의 작성
  if (e.target.classList.contains('btn-primary')) {
    window.location.href = '/mypage/inquiry';
    return;
  }

  // 프로필 수정
  if (e.target.closest('.btn-edit')) {
    window.location.href = '/mypage/profile';
  }
});