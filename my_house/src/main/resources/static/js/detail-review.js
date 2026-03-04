// /static/js/detail-review.js

(function() {
    // 1. 리뷰 목록 로드 함수 (내부 전용)
    async function loadReviews(cid) {
        const list = document.getElementById('reviewList');
        const empty = document.getElementById('reviewEmpty');
        const avgScore = document.getElementById('avgScore');
        const avgStars = document.getElementById('avgStars');
        const reviewCount = document.getElementById('reviewCount');

        if (!list) return; // 요소가 없으면 중단

        try {
            const res = await fetch(`/api/reviews/${cid}`);
            if (!res.ok) throw new Error("리뷰 로드 실패");
            const data = await res.json();

            if (avgScore) avgScore.innerText = data.avgScore || "0.0";
            if (reviewCount) reviewCount.innerText = data.reviewCount || "0";
            if (avgStars) {
                const fullStars = Math.floor(data.avgScore || 0);
                avgStars.innerText = '★'.repeat(fullStars) + '☆'.repeat(5 - fullStars);
            }

            if (!data.list || data.list.length === 0) {
                if (empty) empty.style.display = 'block';
                list.innerHTML = '';
                return;
            }

            if (empty) empty.style.display = 'none';
            list.innerHTML = data.list.map(r => `
                <li style="border-bottom:1px solid #f1f5f9; padding:12px 0; list-style:none;">
                    <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
                        <strong style="font-size:0.9rem; color:#1e293b;">${r.nickname || '익명'}</strong>
                        <span style="color:#f59e0b; font-size:0.8rem;">${'★'.repeat(r.rating)}</span>
                    </div>
                    <p style="font-size:0.85rem; color:#475569; margin:0; line-height:1.4;">${r.content}</p>
                    <small style="font-size:0.75rem; color:#94a3b8; display:block; margin-top:4px;">${r.date}</small>
                </li>
            `).join('');
        } catch (e) {
            console.error("Load Error:", e);
        }
    }

    // 2. 외부에서 호출할 초기화 함수
	window.initReviewModule = function(cid) {
	    console.log("리뷰 초기화 시작, CID:", cid);
	    const reviewForm = document.getElementById('reviewForm');
	    
	    if (!reviewForm) {
	        console.error("reviewForm을 찾을 수 없습니다.");
	        return;
	    }

	    // 1. 별점 로직
	    const starBtns = document.querySelectorAll('.starBtn');
	    const ratingInput = document.getElementById('ratingInput');
	    const ratingText = document.getElementById('ratingText');

	    starBtns.forEach(btn => {
	        btn.onclick = (e) => {
	            e.preventDefault(); // 버튼 클릭 시 폼 제출 방지
	            const score = btn.dataset.score;
	            ratingInput.value = score;
	            ratingText.textContent = `${score}점`;
	            starBtns.forEach(b => {
	                b.style.color = b.dataset.score <= score ? '#f59e0b' : '#cbd5e1';
	            });
	        };
	    });

	    // 2. 폼 제출 로직 (onsubmit 대신 addEventListener 권장)
	    reviewForm.onsubmit = async (e) => {
	        e.preventDefault();
	        console.log("리뷰 제출 시도...");

	        const content = document.getElementById('reviewInput').value;
	        const rating = ratingInput.value;

	        try {
	            const res = await fetch(`/api/reviews/${cid}`, {
	                method: 'POST',
	                headers: { 'Content-Type': 'application/json' },
	                body: JSON.stringify({ rating, content })
	            });

	            console.log("서버 응답 상태:", res.status);

				if (res.ok) {
				    alert('리뷰가 등록되었습니다!');
				    reviewInput.value = '';
				    await loadReviews(cid);
				} else if (res.status === 409) {
				    // ✅ 중복 작성 시 처리
				    alert('이미 리뷰를 작성한 매물입니다. 한 매물당 하나의 리뷰만 작성 가능합니다.');
				} else if (res.status === 401 || res.status === 403) {
				    alert('로그인이 필요한 서비스입니다.');
				} else {
				    alert('에러 발생: ' + res.status);
				}
	        } catch (err) {
	            console.error("Fetch 에러:", err);
	            alert("서버와 통신할 수 없습니다.");
	        }
	    };

	    // 3. 목록 로드 호출
	    loadReviews(cid);
	};
})();