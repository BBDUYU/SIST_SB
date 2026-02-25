// mypage.js

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

  // ✅ SSR 판별 (추천: <body data-ssr="true"> 넣어두기)
  if (isSSR()) {
    // SSR이면 샘플 로딩 금지
    return;
  }

  // ✅ CSR(프론트에서 그릴 때)만 샘플/더미 렌더링
  loadSampleData();

  // (선택) 리뷰 요약도 같이 채우기
  loadReviewSummarySample();
});

// ✅ SSR 판별 함수
function isSSR() {
  // 방법1) HTML에서 window.__SSR__ = true; 심기
  if (window.__SSR__ === true) return true;

  // 방법2) <body data-ssr="true"> 심기 (제일 깔끔)
  const body = document.body;
  if (body && body.dataset && body.dataset.ssr === 'true') return true;

  return false;
}

// 샘플 데이터 로드
function loadSampleData() {
  // 서버에서 이미 카드가 렌더돼 있으면 샘플 넣지 않기
  const hasServerRendered =
    document.querySelector('.property-list .property-card') ||
    document.querySelector('.review-list .review-card') ||
    document.querySelector('.inquiry-list .inquiry-card');

  if (hasServerRendered) return;

  loadRecentProperties();
  loadReviewData();
  loadInquiryData();
}

/* -------------------------
   최근본매물 샘플
------------------------- */
function loadRecentProperties() {
  const propertyList = document.querySelector('.property-list');
  if (!propertyList || propertyList.children.length > 0) return;

  const sampleProperties = [
    {
      id: 1,
      title: '강남 신축 오피스텔',
      location: '서울 강남구',
      price: '3억 2,000만원',
      type: '매매',
      date: '2시간 전',
      image: 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400&h=300&fit=crop'
    },
    {
      id: 2,
      title: '여의도 한강뷰 아파트',
      location: '서울 영등포구',
      price: '15억',
      type: '매매',
      date: '5시간 전',
      image: 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400&h=300&fit=crop'
    },
    {
      id: 3,
      title: '홍대 상가 투룸',
      location: '서울 마포구',
      price: '80만원/월',
      type: '월세',
      date: '1일 전',
      image: 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=400&h=300&fit=crop'
    },
    {
      id: 4,
      title: '판교 테크노밸리 오피스',
      location: '경기 성남시',
      price: '5억',
      type: '매매',
      date: '2일 전',
      image: 'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=400&h=300&fit=crop'
    }
  ];

  propertyList.innerHTML = sampleProperties.map(p => `
    <div class="property-card">
      <img src="${p.image}" alt="${p.title}" class="property-image">
      <div class="property-content">

        <div class="property-header">
          <h3 class="property-title">${p.title}</h3>
          <button class="btn-icon" type="button" aria-label="찜">
            <svg width="20" height="20" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06
                       a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23
                       l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z">
              </path>
            </svg>
          </button>
        </div>

        <!-- ✅ 위치 아이콘 추가 -->
        <div class="property-location">
          <svg width="16" height="16" viewBox="0 0 24 24"
               fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13
                     a9 9 0 0 1 18 0z"></path>
            <circle cx="12" cy="10" r="3"></circle>
          </svg>
          <span>${p.location}</span>
        </div>

        <div class="property-footer">
          <div>
            <span class="property-type">${p.type}</span>
            <span class="property-price">${p.price}</span>
          </div>

          <!-- ✅ 시간 아이콘 추가 -->
          <div class="property-time">
            <svg width="14" height="14" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
            <span>${p.date}</span>
          </div>
        </div>

      </div>
    </div>
  `).join('');
}

/* -------------------------
   리뷰 리스트 샘플
------------------------- */
function loadReviewData() {
  const reviewList = document.querySelector('.review-list');
  if (!reviewList || reviewList.children.length > 0) return;

  const sampleReviews = [
    {
      id: 1,
      property: '강남 신축 오피스텔',
      rating: 5,
      date: '2024.02.15',
      content: '교통이 편리하고 주변 시설이 잘 되어있어 만족스럽습니다. 관리도 깔끔하게 되어있고 추천합니다.',
      likes: 12,
      comments: 3
    },
    {
      id: 2,
      property: '여의도 한강뷰 아파트',
      rating: 4,
      date: '2024.02.10',
      content: '한강 뷰가 정말 좋습니다. 다만 주차 공간이 조금 부족한 편입니다.',
      likes: 8,
      comments: 2
    },
    {
      id: 3,
      property: '홍대 상가 투룸',
      rating: 5,
      date: '2024.02.05',
      content: '위치가 정말 좋고 집도 깨끗합니다. 집주인분도 친절하셔서 좋았어요.',
      likes: 15,
      comments: 5
    }
  ];

  reviewList.innerHTML = sampleReviews.map(review => `
    <div class="card review-card">
      <div class="review-header">
        <div>
          <h4 class="review-property">${review.property}</h4>
          <div class="stars-small">
            ${Array(review.rating).fill('<span class="star filled">★</span>').join('')}
            ${Array(5 - review.rating).fill('<span class="star">★</span>').join('')}
          </div>
        </div>
        <span class="review-date">${review.date}</span>
      </div>

      <p class="review-content">${review.content}</p>

      <!-- ✅ 여기(좋아요/댓글) 다시 추가 -->
      <div class="review-actions">
        <button class="action-btn" type="button" data-action="like" data-id="${review.id}">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3z"></path>
            <path d="M7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"></path>
          </svg>
          <span class="like-count">${review.likes}</span>
        </button>

        <button class="action-btn" type="button" data-action="comment" data-id="${review.id}">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
          <span class="comment-count">${review.comments}</span>
        </button>
      </div>
    </div>
  `).join('');
}

/* -------------------------
   문의 샘플
------------------------- */
function loadInquiryData() {
  const inquiryList = document.querySelector('.inquiry-list');
  if (!inquiryList || inquiryList.children.length > 0) return;

  const sampleInquiries = [
    {
      id: 1,
      title: '계약 조건 문의드립니다',
      property: '강남 신축 오피스텔',
      date: '2024.02.20',
      status: 'answered',
      question: '계약 시 보증금 대출이 가능한가요? 그리고 입주 가능한 날짜는 언제인지 궁금합니다.',
      answer: '보증금 대출은 가능하며, 입주는 3월 1일부터 가능합니다. 자세한 사항은 전화 상담 부탁드립니다.'
    },
    {
      id: 2,
      title: '주차 관련 문의',
      property: '여의도 한강뷰 아파트',
      date: '2024.02.18',
      status: 'answered',
      question: '주차 공간이 충분한지, 그리고 방문자 주차는 어떻게 되는지 알고 싶습니다.',
      answer: '세대당 1대 주차 가능하며, 방문자 주차장은 별도로 운영됩니다. 사전 등록 필요합니다.'
    },
    {
      id: 3,
      title: '매물 방문 일정 문의',
      property: '판교 테크노밸리 오피스',
      date: '2024.02.25',
      status: 'pending',
      question: '이번 주말에 방문이 가능할까요? 토요일 오후 2시쯤 희망합니다.',
      answer: null
    }
  ];

  inquiryList.innerHTML = sampleInquiries.map(inquiry => `
    <div class="card inquiry-card">
      <div class="inquiry-header">
        <div>
          <div class="inquiry-title-row">
            <h4 class="inquiry-title">${inquiry.title}</h4>

            <span class="badge ${inquiry.status === 'answered' ? 'badge-answered' : 'badge-pending'}">
              ${inquiry.status === 'answered'
                ? `
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                       stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                    <polyline points="22 4 12 14.01 9 11.01"></polyline>
                  </svg>
                `
                : `
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                       stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
                `
              }
              <span>${inquiry.status === 'answered' ? '답변완료' : '대기중'}</span>
            </span>
          </div>

          <p class="inquiry-property">${inquiry.property}</p>
          <p class="inquiry-date">${inquiry.date}</p>
        </div>
      </div>

      <div class="question-box">
        <div class="question-content">
          <!-- ✅ 질문 아이콘 -->
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>

          <div>
            <p class="question-label">질문</p>
            <p class="question-text">${inquiry.question}</p>
          </div>
        </div>
      </div>

      ${inquiry.answer ? `
        <div class="answer-box">
          <div class="answer-content">
            <!-- ✅ 답변 아이콘 -->
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>

            <div>
              <p class="answer-label">답변</p>
              <p class="answer-text">${inquiry.answer}</p>
            </div>
          </div>
        </div>
      ` : ''}
    </div>
  `).join('');
}

/* -------------------------
   리뷰 요약 샘플(Th 없이)
   - HTML에서 id로 잡아주면 됨:
   avgScore, avgStars, totalCount, bar5~bar1, percent5~percent1
------------------------- */
function loadReviewSummarySample() {
  // 리뷰 요약 영역이 없으면 패스
  const avgScoreEl = document.getElementById('avgScore');
  const starsEl = document.getElementById('avgStars');
  if (!avgScoreEl || !starsEl) return;

  const stats = {
    averageScore: 4.7,
    totalCount: 38,
    percent5: 60,
    percent4: 25,
    percent3: 10,
    percent2: 3,
    percent1: 2
  };

  avgScoreEl.textContent = Number(stats.averageScore).toFixed(1);

  const totalCountEl = document.getElementById('totalCount');
  if (totalCountEl) totalCountEl.textContent = `(${stats.totalCount}개)`;

  // 별 그리기
  starsEl.innerHTML = '';
  for (let i = 1; i <= 5; i++) {
    const span = document.createElement('span');
    span.className = 'star' + (stats.averageScore >= i ? ' filled' : '');
    span.textContent = '★';
    starsEl.appendChild(span);
  }

  // 막대+퍼센트
  for (let i = 1; i <= 5; i++) {
    const bar = document.getElementById('bar' + i);
    const pct = document.getElementById('percent' + i);
    const v = stats['percent' + i] ?? 0;

    if (bar) bar.style.width = v + '%';
    if (pct) pct.textContent = v + '%';
  }
}

/* -------------------------
   공통 클릭 이벤트
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
    alert('새 문의 작성 페이지로 이동합니다.');
    // window.location.href = '/inquiry/new';
    return;
  }

  // 프로필 수정
  if (e.target.closest('.btn-edit')) {
    alert('프로필 수정 페이지로 이동합니다.');
    // window.location.href = '/profile/edit';
  }
});