// /static/js/main.js
document.addEventListener("DOMContentLoaded", () => {
  // ===== 1) 카카오 지도 =====
  const mapContainer = document.getElementById("map");
  if (mapContainer && window.kakao?.maps) {
    const map = new kakao.maps.Map(mapContainer, {
      center: new kakao.maps.LatLng(37.5665, 126.9780),
      level: 5,
    });

    const marker = new kakao.maps.Marker({
      position: new kakao.maps.LatLng(37.5665, 126.9780),
    });

    marker.setMap(map);

    window.__MAIN_MAP__ = map;
    window.__MAIN_MARKER__ = marker;
  }

  // 알림 점(옵션)
  document.querySelector(".fab-noti")?.classList.add("has-noti");

  // ===== 2) 좌측 패널 SPA (리스트 <-> 상세) =====
  const panel = document.getElementById("panelContent");
  if (!panel) return;

  // ✅ 리스트 화면 전체 백업(필터바 포함)
  const listHTML = panel.innerHTML;

  function animateSwap(nextHTML) {
    panel.classList.remove("panel-enter");
    panel.classList.add("panel-anim", "panel-leave");

    setTimeout(() => {
      panel.innerHTML = nextHTML;

      panel.classList.remove("panel-leave");
      panel.classList.add("panel-enter");

      requestAnimationFrame(() => {
        panel.classList.remove("panel-enter");
      });
    }, 210);
  }

  async function openDetail(propertyKey) {
    const res = await fetch(`/listing/${propertyKey}/panel`, {
      headers: { "X-Requested-With": "fetch" },
    });

    if (!res.ok) {
      console.error("패널 fetch 실패:", res.status);
      return;
    }

    const html = await res.text();
    animateSwap(html);
  }

  function backToList() {
    animateSwap(listHTML);
  }

  // ===== 3) 전역 클릭 이벤트 위임(핵심) =====
  document.addEventListener("click", (e) => {
    // (A) 리스트 하트 토글 (.heart-btn)
    const listHeartBtn = e.target.closest(".heart-btn");
    if (listHeartBtn) {
      e.preventDefault();
      e.stopPropagation(); // house-item 클릭(openDetail) 방지

      const icon = listHeartBtn.querySelector("i");
      if (!icon) return;

      if (icon.classList.contains("bi-heart")) {
        icon.classList.remove("bi-heart");
        icon.classList.add("bi-heart-fill");
        icon.style.color = "#e11d48";
      } else {
        icon.classList.remove("bi-heart-fill");
        icon.classList.add("bi-heart");
        icon.style.color = "";
      }
      return;
    }

    // (B) 상세 하트(SVG) 토글 (#btnHeart.iconBtn)
    const detailHeartBtn = e.target.closest("#btnHeart.iconBtn");
    if (detailHeartBtn) {
      e.preventDefault();
      e.stopPropagation();

      detailHeartBtn.classList.toggle("hearted");

      const path = detailHeartBtn.querySelector("svg path");
      if (!path) return;

      const isOn = detailHeartBtn.classList.contains("hearted");
      if (isOn) {
        path.setAttribute("fill", "#DC2626");
        path.setAttribute("stroke", "#DC2626");
      } else {
        path.setAttribute("fill", "none");
        path.setAttribute("stroke", "#0F172A");
      }
      return;
    }

    // (C) 리스트 카드 클릭 -> 상세 열기
    const item = e.target.closest(".house-item");
    if (item) {
      const key = item.getAttribute("data-key");
      if (!key) return;

      e.preventDefault();
      openDetail(key);
      return;
    }

    // (D) 상세 패널 X 버튼 -> 리스트 복귀
    if (e.target.closest(".panel-close")) {
      e.preventDefault();
      backToList();
      return;
    }

    // (E) 상세 패널 "지도에서 매물 위치로 이동" + 핀 이동
    const recenterBtn = e.target.closest(".panel-recenter");
    if (recenterBtn) {
      const lat = parseFloat(recenterBtn.getAttribute("data-lat"));
      const lng = parseFloat(recenterBtn.getAttribute("data-lng"));
      if (Number.isNaN(lat) || Number.isNaN(lng)) return;

      if (window.__MAIN_MAP__ && window.__MAIN_MARKER__ && window.kakao?.maps) {
        const center = new kakao.maps.LatLng(lat, lng);
        window.__MAIN_MAP__.setCenter(center);
        window.__MAIN_MAP__.setLevel(3);

        window.__MAIN_MARKER__.setPosition(center);
        window.__MAIN_MARKER__.setMap(window.__MAIN_MAP__);
      }
      return;
    }
  });
})();


