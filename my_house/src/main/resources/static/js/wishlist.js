function getCsrf() {
  const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
  return { token, header };
}

document.addEventListener('click', async (e) => {
  const btn = e.target.closest('.btn-remove');
  if (!btn) return;

  e.preventDefault();
  e.stopPropagation();
  e.stopImmediatePropagation();

  const id = btn.dataset.id;
  if (!id) {
    console.error("삭제 버튼 data-id 없음");
    return;
  }

  if (!confirm('위시리스트에서 삭제할까요?')) return;

  const { token, header } = getCsrf();

  const res = await fetch(`/mypage/wishlist/remove?id=${id}`, {
    method: 'POST',
    headers: {
      ...(token && header ? { [header]: token } : {}),
      'X-Requested-With': 'XMLHttpRequest'
    },
    credentials: 'same-origin'
  });

  if (!res.ok) {
    console.error("삭제 실패:", res.status);
    alert('삭제 실패');
    return;
  }

  btn.closest('.wishlist-card')?.remove();
});