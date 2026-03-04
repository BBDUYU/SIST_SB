document.addEventListener("DOMContentLoaded", () => {
  // 현재 경로 기반 active 처리
  const path = location.pathname;
  document.querySelectorAll(".admin-nav a").forEach(a => {
    const href = a.getAttribute("href");
    if (href && path === href) a.classList.add("active");
  });

  // 검색 input(일단 UI만): Enter 누르면 q 파라미터로 이동
  document.querySelectorAll("[data-admin-search]").forEach(input => {
    input.addEventListener("keydown", (e) => {
      if (e.key !== "Enter") return;
      e.preventDefault();

      const q = input.value.trim();
      const url = new URL(location.href);
      if (q) url.searchParams.set("q", q);
      else url.searchParams.delete("q");
      url.searchParams.set("page", "0");
      location.href = url.toString();
    });
  });
  const saveBtn = document.querySelector('button[type="button"].btn-primary');
    if (saveBtn && saveBtn.textContent.includes('저장')) {
        saveBtn.addEventListener('click', saveAnswer);
    }
});



let currentInquiryId = null;

function prepareAnswer(id, title) {
    currentInquiryId = id;
    const textarea = document.querySelector('textarea');
    textarea.focus();
    textarea.placeholder = `[${title}] 에 대한 답변을 입력하세요.`;
}

async function saveAnswer() {
    if (!currentInquiryId) {
        alert("답변할 질문을 선택해주세요.");
        return;
    }
    const content = document.querySelector('textarea').value;
    if (!content.trim()) {
        alert("내용을 입력하세요.");
        return;
    }

    // --- [추가 시작] 헤드에서 CSRF 토큰과 헤더 이름 읽기 ---
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    // --- [추가 끝] ---

    const formData = new URLSearchParams();
    formData.append('id', currentInquiryId);
    formData.append('content', content);

    try {
        const res = await fetch('/admin/qna/answer', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/x-www-form-urlencoded',
                // ✅ 읽어온 토큰을 헤더에 실어 보냅니다.
                [csrfHeader]: csrfToken 
            },
            body: formData
        });

        if (res.ok) {
            alert("답변이 성공적으로 저장되었습니다.");
            location.reload(); // 성공 시 리스트 갱신
        } else {
            const errorMsg = await res.text();
            alert("저장 실패: " + errorMsg);
        }
    } catch (error) {
        console.error("통신 에러:", error);
        alert("서 bir 와의 연결에 실패했습니다.");
    }
}