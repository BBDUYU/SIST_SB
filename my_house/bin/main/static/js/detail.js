(function () {
  const data = window.__DETAIL__;
  if (!data) return;

  const { lat, lng } = data;

  const mapContainer = document.getElementById('detailMap');
  if (!mapContainer) return;

  const center = new kakao.maps.LatLng(lat, lng);
  const map = new kakao.maps.Map(mapContainer, { center, level: 3 });

  const marker = new kakao.maps.Marker({ position: center });
  marker.setMap(map);

  // toast helper
  const toastEl = document.getElementById('toast');
  let toastTimer = null;
  function toast(msg) {
    if (!toastEl) return;
    toastEl.textContent = msg;
    toastEl.classList.add('show');
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toastEl.classList.remove('show'), 1200);
  }

  // Wishlist toggle (dummy)
  const btnHeart = document.getElementById('btnHeart');
  btnHeart?.addEventListener('click', function () {
    btnHeart.classList.toggle('hearted');
    toast(btnHeart.classList.contains('hearted') ? '위시리스트에 추가' : '위시리스트 해제');
  });

  // Share (copy URL)
  const btnShare = document.getElementById('btnShare');
  btnShare?.addEventListener('click', async function () {
    try {
      await navigator.clipboard.writeText(window.location.href);
      toast('링크 복사됨');
    } catch (e) {
      const tmp = document.createElement('textarea');
      tmp.value = window.location.href;
      document.body.appendChild(tmp);
      tmp.select();
      document.execCommand('copy');
      document.body.removeChild(tmp);
      toast('링크 복사됨');
    }
  });

  // Map buttons
  document.getElementById('btnRecenter')?.addEventListener('click', function () {
    map.setCenter(center);
    map.setLevel(3);
    toast('매물 위치로 이동');
  });

  document.getElementById('btnMyPos')?.addEventListener('click', function () {
    const my = new kakao.maps.LatLng(lat + 0.002, lng + 0.002);
    map.panTo(my);
    toast('내 위치(더미) 이동');
  });
})();