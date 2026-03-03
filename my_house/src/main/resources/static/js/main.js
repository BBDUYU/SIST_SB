// /static/js/main.js
document.addEventListener("DOMContentLoaded", () => {
  // =========================
  // 0) 유틸
  // =========================
  const $ = (sel) => document.querySelector(sel);

  // =========================
  // 1) 카카오 지도 생성
  // =========================
  const mapContainer = document.getElementById("map");
  if (mapContainer && window.kakao?.maps) {
    const map = new kakao.maps.Map(mapContainer, {
      center: new kakao.maps.LatLng(37.5665, 126.9780),
      level: 5,
    });

    // "중앙 이동용" 메인 마커(상세 패널의 recenter 버튼에서 사용)
    const mainMarker = new kakao.maps.Marker({
      position: new kakao.maps.LatLng(37.5665, 126.9780),
    });
    mainMarker.setMap(map);

    window.__MAIN_MAP__ = map;
    window.__MAIN_MARKER__ = mainMarker;

    // 추가 레이어(공고/귀갓길/CCTV) 초기화
    initAdditionalLayers(map);

    // 단지(내 DB) 리스트 + 마커 초기화
    initComplexBoundsSync(map);
  }

  // 알림 점(옵션)
  document.querySelector(".fab-noti")?.classList.add("has-noti");

  // =========================
  // 2) 좌측 패널 SPA (리스트 <-> 상세)
  // =========================
  const panel = document.getElementById("panelContent");
  if (!panel) return;

  // ✅ 리스트 화면 전체 백업(필터바 포함)
  let listHTML = panel.innerHTML;

  function animateSwap(nextHTML, afterSwap) {
    panel.classList.remove("panel-enter");
    panel.classList.add("panel-anim", "panel-leave");

    setTimeout(() => {
      panel.innerHTML = nextHTML;

      panel.classList.remove("panel-leave");
      panel.classList.add("panel-enter");

      requestAnimationFrame(() => {
        panel.classList.remove("panel-enter");
      });

      if (typeof afterSwap === "function") afterSwap();
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
    animateSwap(listHTML, () => {
      // 리스트 화면으로 돌아온 직후, bounds 기준으로 다시 렌더
      if (window.__MAIN_MAP__) {
        // listHTML이 오래된 상태일 수 있어 최신 DOM으로 다시 갱신
        listHTML = panel.innerHTML;
        window.__UPDATE_COMPLEX_BY_BOUNDS__?.();
      }
    });
  }

  // =========================
  // 3) 전역 클릭 이벤트 위임(핵심)
  // =========================
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

  // =========================
  // 4) 단지 bounds 동기화 (내 DB complex) + 로드뷰 썸네일
  // =========================
// ✅ initComplexBoundsSync(map) : 줌 멀어지면 매물(리스트/마커) 숨김 + 가까워지면 다시 로드
function initComplexBoundsSync(map) {
  const complexMarkerImage = new kakao.maps.MarkerImage(
    "/image/pin.png",
    new kakao.maps.Size(48, 48),
    { offset: new kakao.maps.Point(24, 48) }
  );
  
  let complexMarkers = [];
  let debounceTimer = null;

  // 숫자 클수록 멀리 보임 (1~14)
  const MAX_LEVEL_FOR_COMPLEX = 5; // ✅ 5보다 멀어지면 매물 숨김 (원하면 4/6으로 조절)

  function ensureListContainer() {
    let el = document.getElementById("complexList");
    if (el) return el;

    const body = panel.querySelector(".panel-body");
    if (!body) return null;

    el = document.createElement("div");
    el.id = "complexList";
    body.appendChild(el);
    return el;
  }

  function clearComplexMarkers() {
    complexMarkers.forEach((m) => m.setMap(null));
    complexMarkers = [];
  }

  function hideComplexUI(msg = "지도를 더 확대하면 매물이 표시돼요") {
    clearComplexMarkers();

    const el = ensureListContainer();
    if (el) {
      el.innerHTML = `<div class="text-muted p-3">${msg}</div>`;
    }
  }

  function getBoundsParams() {
    const bounds = map.getBounds();
    const sw = bounds.getSouthWest();
    const ne = bounds.getNorthEast();
    return {
      swLat: sw.getLat(),
      swLng: sw.getLng(),
      neLat: ne.getLat(),
      neLng: ne.getLng(),
    };
  }

  async function fetchComplexInBounds() {
    const p = getBoundsParams();
    const url = `/api/complex/in-bounds?swLat=${p.swLat}&swLng=${p.swLng}&neLat=${p.neLat}&neLng=${p.neLng}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error("in-bounds API 실패: " + res.status);
    const data = await res.json();
    return Array.isArray(data) ? data : [];
  }

  function renderComplexMarkers(items) {
    clearComplexMarkers();

    items.forEach((it) => {
      if (it.latitude == null || it.longitude == null) return;

      const marker = new kakao.maps.Marker({
        map,
        position: new kakao.maps.LatLng(it.latitude, it.longitude),
		image: complexMarkerImage,
      });

      kakao.maps.event.addListener(marker, "click", () => {
        if (it.cid != null) openDetail(String(it.cid));
      });

      complexMarkers.push(marker);
    });
  }

  function renderComplexList(items) {
    const el = ensureListContainer();
    if (!el) return;

    if (!items || items.length === 0) {
      el.innerHTML = `<div class="text-muted p-3">현재 지도 범위에 단지가 없어요</div>`;
      return;
    }

    // ✅ 썸네일은 일단 기존 껍데기 유지(필요하면 나중에 img/StaticMap 붙이기)
    el.innerHTML = items
      .map(
        (it) => `
        <div class="card house-card mb-3 house-item"
             data-key="${it.cid}"
             data-lat="${it.latitude}"
             data-lng="${it.longitude}">
          <div class="card-body d-flex gap-3 align-items-center">
            <div class="thumb"></div>

            <div class="flex-grow-1">
              <div class="fw-semibold">${it.title ?? ""}</div>
              <div class="small text-muted">${it.address ?? ""}</div>
            </div>

            <a href="#" class="heart-btn" aria-label="찜">
              <i class="bi bi-heart"></i>
            </a>
          </div>
        </div>
      `
      )
      .join("");
  }

  async function updateByBounds() {
    try {
      // ✅ 멀리 보면 조회 자체 중지 + UI 숨김
      if (map.getLevel() > MAX_LEVEL_FOR_COMPLEX) {
        hideComplexUI(`지도를 더 확대하면 매물이 표시돼요 (현재 레벨 ${map.getLevel()})`);
        return;
      }

      const data = await fetchComplexInBounds();
      renderComplexMarkers(data);

      if (document.getElementById("complexList")) {
        renderComplexList(data);
      }
    } catch (e) {
      console.error(e);
    }
  }

  function onMapChanged() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(updateByBounds, 200);
  }

  // ✅ drag/zoom 둘 다 커버 + 호출도 적은 이벤트
  kakao.maps.event.addListener(map, "idle", onMapChanged);

  // 리스트 복귀 시 외부에서 강제 갱신
  window.__UPDATE_COMPLEX_BY_BOUNDS__ = updateByBounds;

  // 최초 1회
  updateByBounds();
}

  // =========================
  // 5) LH 공고/안심귀갓길/CCTV 레이어
  // =========================
  function initAdditionalLayers(map) {
    // 5-1) LH 공고
    fetch("/api/notices")
      .then((res) => res.json())
      .then((data) => {
        if (!Array.isArray(data)) return;
        data.forEach((notice) => {
          if (notice.latitude && notice.longitude) {
            displayLhMarker(notice, map);
          }
        });
      })
      .catch((err) => console.error("공고 데이터 로딩 실패:", err));

    // 5-2) 안심귀갓길(Polyline)
    fetch("/api/safe-paths")
      .then((res) => res.json())
      .then((paths) => {
        if (!Array.isArray(paths)) return;
        paths.forEach((path) => drawSafePolyline(path, map));
      })
      .catch((err) => console.error("안심귀갓길 로딩 실패:", err));

    // 5-3) CCTV(줌에 따라 bounds 요청)
    kakao.maps.event.addListener(map, "idle", () => {
      if (map.getLevel() <= 4) updateCctvMarkers(map);
      else {
        cctvMarkers.forEach((m) => m.setMap(null));
      }
    });
  }

  // =========================
  // CCTV 마커
  // =========================
  let cctvMarkers = [];

  function updateCctvMarkers(map) {
    const bounds = map.getBounds();
    const sw = bounds.getSouthWest();
    const ne = bounds.getNorthEast();

    fetch(
      `/api/cctv?minLat=${sw.getLat()}&maxLat=${ne.getLat()}&minLng=${sw.getLng()}&maxLng=${ne.getLng()}`
    )
      .then((res) => {
        if (!res.ok) throw new Error("서버 응답 에러");
        return res.json();
      })
      .then((data) => {
        cctvMarkers.forEach((m) => m.setMap(null));
        cctvMarkers = [];

        if (!Array.isArray(data)) {
          console.warn("CCTV 데이터 형식이 배열이 아닙니다:", data);
          return;
        }

        data.forEach((cctv) => {
          const marker = new kakao.maps.Marker({
            position: new kakao.maps.LatLng(cctv.latitude, cctv.longitude),
            image: new kakao.maps.MarkerImage(
              "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png",
              new kakao.maps.Size(20, 30)
            ),
          });

          const iwContent = `
            <div style="padding:10px; min-width:150px; border-radius:8px;">
              <div style="font-weight:bold; color:#1e293b; margin-bottom:4px;">📷 CCTV 정보</div>
              <div style="font-size:12px; color:#475569;">용도: <b>${cctv.purposeDesc}</b></div>
              <div style="font-size:12px; color:#475569;">대수: <b>${cctv.count || 0}대</b></div>
              <div style="font-size:11px; color:#94a3b8; margin-top:4px;">📍 ${cctv.agency}</div>
            </div>
          `;

          const infowindow = new kakao.maps.InfoWindow({
            content: iwContent,
            disableAutoPan: true,
          });

          kakao.maps.event.addListener(marker, "mouseover", () => infowindow.open(map, marker));
          kakao.maps.event.addListener(marker, "mouseout", () => infowindow.close());

          marker.setMap(map);
          cctvMarkers.push(marker);
        });
      })
      .catch((err) => console.error("CCTV 로딩 중 에러 발생:", err));
  }

  // =========================
  // LH 마커/오버레이
  // =========================
  function displayLhMarker(notice, map) {
    const position = new kakao.maps.LatLng(notice.latitude, notice.longitude);

    const marker = new kakao.maps.Marker({
      map,
      position,
    });

    const content = `
      <div style="background:white; border:1px solid #28a745; padding:2px 6px;
                  font-size:11px; font-weight:bold; color:#28a745;
                  border-radius:12px; transform:translateY(-40px); white-space:nowrap;
                  box-shadow:0 2px 4px rgba(0,0,0,0.1); pointer-events:none;">
        ${notice.aisTpCdNm}
      </div>
    `;

    new kakao.maps.CustomOverlay({
      position,
      content,
      map,
    });

    const iwContent = `
      <div style="padding:15px; width:250px;">
        <div style="font-size:14px; font-weight:bold; margin-bottom:8px;">${notice.panNm}</div>
        <div style="font-size:12px; color:#666; margin-bottom:10px;">📅 마감: ${notice.clsgDt}</div>
        <a href="${notice.dtlUrl}" target="_blank"
           style="display:block; background:#28a745; color:#fff; text-decoration:none;
                  text-align:center; padding:8px; border-radius:4px; font-size:12px;">
          공고 상세보기
        </a>
      </div>
    `;

    const infowindow = new kakao.maps.InfoWindow({
      content: iwContent,
      removable: true,
    });

    kakao.maps.event.addListener(marker, "click", () => infowindow.open(map, marker));
  }

  // =========================
  // 안심귀갓길 Polyline + Tooltip
  // =========================
  function drawSafePolyline(path, map) {
    try {
      const rawCoords = JSON.parse(path.pathCoordinates);
      const linePath = rawCoords.map((p) => new kakao.maps.LatLng(p[1], p[0]));

      const polyline = new kakao.maps.Polyline({
        path: linePath,
        strokeWeight: 6,
        strokeColor: "#2ECC71",
        strokeOpacity: 0.7,
        strokeStyle: "solid",
      });

      polyline.setMap(map);

      const addressParts = [path.sigungu, path.bjdName, path.detailLocation];
      const fullAddr = addressParts
        .filter((part) => part && part !== "null" && part.trim() !== "")
        .join(" ");

      const tooltipContent = `
        <div class="safe-tooltip" style="background:rgba(0,0,0,0.85); color:white; padding:10px 15px; border-radius:10px; font-size:12px; pointer-events:none; z-index:1000; min-width:180px;">
          <div style="font-weight:bold; margin-bottom:6px; color:#2ECC71; border-bottom:1px solid #444; padding-bottom:4px;">
            🛡️ 안심귀갓길 정보
          </div>
          ${fullAddr ? `<div style="margin-bottom:8px; color:#ddd; font-size:11px;">📍 ${fullAddr}</div>` : ""}
          <div style="display:flex; justify-content:space-between; gap:10px;">
            <span>🚨 벨: <b>${path.bellCount || 0}</b></span>
            <span>📷 CCTV: <b>${path.cctvCount || 0}</b></span>
            <span>💡 보안등: <b>${path.lampCount || 0}</b></span>
          </div>
        </div>
      `;

      const tooltip = new kakao.maps.CustomOverlay({
        content: tooltipContent,
        xAnchor: 0.5,
        yAnchor: 1.3,
      });

      kakao.maps.event.addListener(polyline, "mouseover", (mouseEvent) => {
        polyline.setOptions({ strokeOpacity: 1.0, strokeWeight: 8, strokeColor: "#27AE60" });
        tooltip.setPosition(mouseEvent.latLng);
        tooltip.setMap(map);
      });

      kakao.maps.event.addListener(polyline, "mousemove", (mouseEvent) => {
        tooltip.setPosition(mouseEvent.latLng);
      });

      kakao.maps.event.addListener(polyline, "mouseout", () => {
        polyline.setOptions({ strokeOpacity: 0.7, strokeWeight: 6, strokeColor: "#2ECC71" });
        tooltip.setMap(null);
      });
    } catch (e) {
      console.error("안심귀갓길 툴팁 렌더링 에러:", e);
    }
  }
  
  // ---- StaticMap 썸네일(공식) ----
  const staticObserver = new IntersectionObserver((entries, obs) => {
    entries.forEach(entry => {
      if (!entry.isIntersecting) return;

      const el = entry.target; // .static-thumb
      if (el.dataset.loaded) { obs.unobserve(el); return; }

      const lat = parseFloat(el.dataset.lat);
      const lng = parseFloat(el.dataset.lng);
      if (Number.isNaN(lat) || Number.isNaN(lng)) { obs.unobserve(el); return; }

      const center = new kakao.maps.LatLng(lat, lng);
      const marker = { position: center };

      new kakao.maps.StaticMap(el, {
        center,
        level: 3,
        marker
      });

      el.dataset.loaded = "1";
      obs.unobserve(el);
    });
  }, { root: null, threshold: 0.1 });

  function lazyLoadStaticThumbs() {
    document.querySelectorAll(".static-thumb").forEach(el => staticObserver.observe(el));
  }
});