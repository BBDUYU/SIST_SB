// 페이지 로드 시 오늘 날짜 표시
document.addEventListener('DOMContentLoaded', function() {
    // 오늘 날짜 설정
    const today = new Date();
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    const formattedDate = today.toLocaleDateString('ko-KR', options);
    
    document.getElementById('createdDate').textContent = formattedDate;

    // 폼 제출 이벤트
    const form = document.getElementById('inquiryForm');
    form.addEventListener('submit', handleSubmit);

    // 취소 버튼 이벤트
    const cancelBtn = document.getElementById('cancelBtn');
    cancelBtn.addEventListener('click', handleCancel);
});

// 폼 제출 처리
function handleSubmit(e) {
    e.preventDefault();

    const formData = {
        createdDate: document.getElementById('createdDate').textContent,
        title: document.getElementById('title').value,
        content: document.getElementById('content').value
    };

    console.log('제출된 데이터:', formData);

    // 여기에 실제 서버 전송 로직 추가
    // 예: fetch('/api/inquiry', { method: 'POST', body: JSON.stringify(formData) })

    alert('문의가 성공적으로 등록되었습니다.');
    
    // 폼 초기화 (선택사항)
    // document.getElementById('inquiryForm').reset();
}

// 취소 버튼 처리
function handleCancel() {
    if (confirm('작성을 취소하시겠습니까?')) {
        // 폼 초기화
        document.getElementById('inquiryForm').reset();
        
        // 또는 이전 페이지로 이동
        // window.history.back();
        
        // 또는 특정 페이지로 이동
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
