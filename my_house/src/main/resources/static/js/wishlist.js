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

// ✅ 위시리스트 로드뷰 정적 썸네일 로드
function lazyLoadWishlistRoadview() {

  if (!window.kakao?.maps?.RoadviewClient) return;

  const rvClient = new kakao.maps.RoadviewClient();

  document.querySelectorAll(".rv-thumb-container").forEach((box) => {

    const lat = Number(box.dataset.lat);
    const lng = Number(box.dataset.lng);

    if (!lat || !lng) return;

    const position = new kakao.maps.LatLng(lat, lng);

    rvClient.getNearestPanoId(position, 50, (panoId) => {

      if (!panoId) return;

      const img = document.createElement("img");

      img.style.width = "100%";
      img.style.height = "100%";
      img.style.objectFit = "cover";

      box.innerHTML = "";
      box.appendChild(img);

    });

  });

}

document.addEventListener("DOMContentLoaded", () => {
  if (!window.kakao?.maps?.Roadview || !window.kakao?.maps?.RoadviewClient) return;

  document.querySelectorAll(".rv-thumb-container").forEach((box) => {
    const lat = Number(box.dataset.lat);
    const lng = Number(box.dataset.lng);
    if (!lat || !lng) return;

    box.innerHTML = "";
    box.style.pointerEvents = "none"; // 썸네일 드래그 방지

    const rv = new kakao.maps.Roadview(box);
    const rvClient = new kakao.maps.RoadviewClient();
    const position = new kakao.maps.LatLng(lat, lng);

    rvClient.getNearestPanoId(position, 50, (panoId) => {
      if (!panoId) {
        box.innerHTML = '<i class="bi bi-geo-alt-fill text-muted opacity-50"></i>';
        return;
      }
      rv.setPanoId(panoId, position);
    });
  });
});