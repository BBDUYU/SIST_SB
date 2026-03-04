/**
 * 회원 관리 전용 모듈
 */
const adminMemberModule = (function() {
    const sendRequest = async (url, params) => {
        const csrfTokenEl = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderEl = document.querySelector('meta[name="_csrf_header"]');

        if (!csrfTokenEl || !csrfHeaderEl) {
            alert("보안 토큰(CSRF)을 찾을 수 없습니다. 페이지를 새로고침 하세요.");
            return;
        }

        const csrfToken = csrfTokenEl.getAttribute('content');
        const csrfHeader = csrfHeaderEl.getAttribute('content');
        const formData = new URLSearchParams();
        for (const key in params) { formData.append(key, params[key]); }

        try {
            const res = await fetch(url, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader]: csrfToken 
                },
                body: formData
            });

            if (res.ok) {
                location.reload();
            } else {
                const errorMsg = await res.text();
                alert("요청 실패: " + errorMsg);
                location.reload();
            }
        } catch (error) {
            console.error("통신 에러:", error);
            alert("서버와의 연결에 실패했습니다.");
        }
    };

    return {
        toggleStatus: (btn) => {
            const uid = btn.dataset.uid;
            const currentStatus = btn.dataset.currentStatus;
            const nextStatus = (currentStatus === 'ACTIVE') ? 'BANNED' : 'ACTIVE';
            if (confirm(`상태를 ${nextStatus === 'BANNED' ? '정지' : '정상'}로 변경하시겠습니까?`)) {
                sendRequest('/admin/members/update-status', { userId: uid, status: nextStatus });
            }
        },
        toggleRole: (btn) => {
            const uid = btn.dataset.uid;
            const currentRole = btn.dataset.currentRole;
            const nextRole = (currentRole === 'ROLE_USER') ? 'ROLE_ADMIN' : 'ROLE_USER';
            if (confirm(`권한을 ${nextRole}(으)로 변경하시겠습니까?`)) {
                sendRequest('/admin/members/update-role', { userId: uid, role: nextRole });
            }
        },
        changeStatus: function(uid, targetStatus) {
            if (confirm(`사용자 상태를 ${targetStatus}(으)로 변경하시겠습니까?`)) {
                sendRequest('/admin/members/update-status', { userId: uid, status: targetStatus });
            } else {
                location.reload();
            }
        }
    };
})();

/**
 * 페이지 로드 시 초기화
 */
document.addEventListener("DOMContentLoaded", () => {
    // 1. 네비게이션 활성화
    const path = location.pathname;
    document.querySelectorAll(".admin-nav a").forEach(a => {
        if (a.getAttribute("href") === path) a.classList.add("active");
    });

    // 2. 검색 엔터 처리
    document.querySelectorAll("[data-admin-search]").forEach(input => {
        input.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                const q = input.value.trim();
                const url = new URL(location.href);
                q ? url.searchParams.set("q", q) : url.searchParams.delete("q");
                url.searchParams.set("page", "0");
                location.href = url.toString();
            }
        });
    });

    // 3. 새 관리자 등록 버튼 (중요: 함수 밖으로 독립)
    const registerAdminBtn = document.querySelector('.btn-primary.btn-sm');
    if (registerAdminBtn && registerAdminBtn.textContent.includes('새 관리자 등록')) {
        registerAdminBtn.addEventListener('click', () => {
            location.href = '/admin/members/register';
        });
    }

    // 4. Q&A 저장 버튼
    const saveBtn = document.querySelector('button[type="button"].btn-primary');
    if (saveBtn && saveBtn.textContent.includes('저장')) {
        saveBtn.addEventListener('click', saveAnswer);
    }
});

// Q&A 관련 함수 (독립 선언)
let currentInquiryId = null;
function prepareAnswer(id, title) {
    currentInquiryId = id;
    const textarea = document.querySelector('textarea');
    if(textarea) {
        textarea.focus();
        textarea.placeholder = `[${title}] 에 대한 답변을 입력하세요.`;
    }
}

async function saveAnswer() {
    if (!currentInquiryId) return alert("질문을 선택하세요.");
    const content = document.querySelector('textarea').value;
    if (!content.trim()) return alert("내용을 입력하세요.");

    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const formData = new URLSearchParams();
    formData.append('id', currentInquiryId);
    formData.append('content', content);

    try {
        const res = await fetch('/admin/qna/answer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken },
            body: formData
        });
        if (res.ok) { alert("성공!"); location.reload(); }
        else { alert("실패: " + await res.text()); }
    } catch (e) { alert("연결 에러"); }
}