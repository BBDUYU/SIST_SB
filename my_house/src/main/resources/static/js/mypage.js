// mypage.js (정리본: 더미 제거 + 탭 + 통계탭 이동 + 하트토글 + 리뷰바 계산)

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

      // ✅ 리뷰 탭으로 이동했을 때도 바 다시 계산(안전)
      if (tabName === 'review') {
        initReviewBarsFromDom();
      }
    });
  });

  // 활동 통계 클릭 -> 해당 탭으로 이동
  document.querySelectorAll('[data-go-tab]').forEach(el => {
    el.addEventListener('click', () => {
      const tabKey = el.getAttribute('data-go-tab');
      if (!tabKey) return;

      const targetBtn = document.querySelector(`.tab-button[data-tab="${tabKey}"]`);
      if (targetBtn) targetBtn.click();
    });
  });

  // ✅ 리뷰 막대(퍼센트) 계산/세팅
  initReviewBarsFromDom();

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

  // ✅ 핵심: 리뷰 카드 DOM에서 rating 분포를 계산해서 bar/percent 반영
  function initReviewBarsFromDom() {
    const reviewCards = document.querySelectorAll('#review-tab .review-card');
    if (!reviewCards || reviewCards.length === 0) {
      // 리뷰가 없으면 전부 0으로
      for (let i = 1; i <= 5; i++) setBar(i, 0);
      return;
    }

    // 별점 카운트(1~5)
    const counts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };

    reviewCards.forEach(card => {
      // review-card 안에 stars-small이 있고, filled 붙은 별 개수로 rating 계산
      const stars = card.querySelectorAll('.stars-small .star');
      if (!stars || stars.length === 0) return;

      const filled = card.querySelectorAll('.stars-small .star.filled').length;
      if (filled >= 1 && filled <= 5) counts[filled] += 1;
    });

    const total = Object.values(counts).reduce((a, b) => a + b, 0);

    for (let score = 5; score >= 1; score--) {
      const pct = total === 0 ? 0 : Math.round((counts[score] / total) * 100);
      setBar(score, pct);
    }
  }

  function setBar(score, pct) {
    const bar = document.getElementById('bar' + score);
    const percent = document.getElementById('percent' + score);
    if (!bar || !percent) return;

    bar.style.width = pct + '%';
    percent.textContent = pct + '%';
    bar.classList.toggle('has-value', pct > 0);
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