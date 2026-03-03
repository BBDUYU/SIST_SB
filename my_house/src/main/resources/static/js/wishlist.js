document.addEventListener('click', (e) => {
  const removeBtn = e.target.closest('.btn-remove');
  if (!removeBtn) return;

  const id = removeBtn.dataset.id;
  if (!confirm('위시리스트에서 삭제할까요?')) return;

  // TODO: 나중에 API 붙이면 fetch로 삭제
  // fetch(`/api/wishlist/${id}`, { method: 'DELETE' })

  // 임시: 화면에서만 제거
  const card = removeBtn.closest('.wishlist-card');
  card?.remove();
});