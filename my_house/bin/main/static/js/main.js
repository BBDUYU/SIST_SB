// 하트 토글 기능
document.addEventListener("DOMContentLoaded", function () {

    document.querySelectorAll(".heart-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const icon = this.querySelector("i");

            if (icon.classList.contains("bi-heart")) {
                icon.classList.remove("bi-heart");
                icon.classList.add("bi-heart-fill");
                icon.style.color = "#e11d48";
            } else {
                icon.classList.remove("bi-heart-fill");
                icon.classList.add("bi-heart");
                icon.style.color = "";
            }
        });
    });

});

document.addEventListener("DOMContentLoaded", function () {

    // 카카오 지도 생성
    const mapContainer = document.getElementById('map');

    const mapOption = {
        center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울 시청
        level: 5
    };

    const map = new kakao.maps.Map(mapContainer, mapOption);

    // 테스트 마커
    const markerPosition = new kakao.maps.LatLng(37.5665, 126.9780);

    const marker = new kakao.maps.Marker({
        position: markerPosition
    });

    marker.setMap(map);
	
	window.__MAIN_MAP__ = map;
	window.__MAIN_MARKER__ = marker;

});

document.querySelector('.fab-noti')?.classList.add('has-noti');   // 점 켜기
// document.querySelector('.fab-noti')?.classList.remove('has-noti'); // 점 끄기

(function () {
  const panel = document.getElementById('panelContent');
  if (!panel) return;

  // 최초 리스트 HTML 저장 (X 눌렀을 때 복귀)
  const listHTML = panel.innerHTML;

  // 패널 애니메이션 유틸
  function animateSwap(nextHTML) {
    panel.classList.add('panel-anim');
    panel.classList.add('panel-leave');

    setTimeout(() => {
      panel.innerHTML = nextHTML;
      panel.classList.remove('panel-leave');
      panel.classList.add('panel-enter');

      // 다음 프레임에 enter 제거 -> 자연스럽게 들어옴
      requestAnimationFrame(() => {
        panel.classList.remove('panel-enter');
      });
    }, 210);
  }

  async function openDetail(propertyKey) {
    const res = await fetch(`/listing/${propertyKey}/panel`, { headers: { 'X-Requested-With': 'fetch' } });
    const html = await res.text();
    animateSwap(html);
  }

  function backToList() {
    animateSwap(listHTML);
  }

  // 리스트 카드 클릭 -> 상세 패널 열기
  document.addEventListener('click', (e) => {
    // 하트 같은 버튼은 무시
    if (e.target.closest('.heart-btn')) return;

    const item = e.target.closest('.house-item');
    if (!item) return;

    const key = item.getAttribute('data-key');
    if (!key) return;

    e.preventDefault();
    openDetail(key);
  });

  // 상세 패널 X 버튼 -> 리스트 복귀
  document.addEventListener('click', (e) => {
    if (e.target.closest('.panel-close')) {
      e.preventDefault();
      backToList();
    }
  });

  // (옵션) 상세 패널의 "지도 이동" 버튼
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('.panel-recenter');
    if (!btn) return;

    const lat = parseFloat(btn.getAttribute('data-lat'));
    const lng = parseFloat(btn.getAttribute('data-lng'));
    if (Number.isNaN(lat) || Number.isNaN(lng)) return;

    // main.js에서 만든 전역 map이 있다면 그걸 사용
    if (window.__MAIN_MAP__ && window.kakao?.maps) {
      const center = new kakao.maps.LatLng(lat, lng);
      window.__MAIN_MAP__.setCenter(center);
      window.__MAIN_MAP__.setLevel(3);
	  
	  // ✅ 핀도 같이 이동
      if (window.__MAIN_MARKER__) {
        window.__MAIN_MARKER__.setPosition(center);
        window.__MAIN_MARKER__.setMap(window.__MAIN_MAP__); // 혹시 숨겨져있을 수 있으니 보이게
      }
    }
  });
})();