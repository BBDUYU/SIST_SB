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
})

// --- [LH 공고 핀 & 안심귀갓길 통합 로직] ---
document.addEventListener("DOMContentLoaded", function () {
    // 위에서 생성된 window.__MAIN_MAP__이 잡힐 때까지 잠시 대기하거나 바로 실행
    const checkMap = setInterval(() => {
        if (window.__MAIN_MAP__) {
            clearInterval(checkMap);
            initAdditionalLayers(window.__MAIN_MAP__);
        }
    }, 100);
});

function initAdditionalLayers(map) {
    // 1. LH 공고 데이터 가져오기 (마커)
    fetch('/api/notices')
        .then(res => res.json())
        .then(data => {
            data.forEach(notice => {
                if (notice.latitude && notice.longitude) {
                    displayLhMarker(notice, map);
                }
            });
        })
        .catch(err => console.error("공고 데이터 로딩 실패:", err));

    // 2. 안심귀갓길 데이터 가져오기 (선 - SHP 변환본)
    fetch('/api/safe-paths') // 안심귀갓길 리스트를 주는 API 주소
        .then(res => res.json())
        .then(paths => {
            paths.forEach(path => {
                drawSafePolyline(path, map);
            });
        })
        .catch(err => console.error("안심귀갓길 로딩 실패:", err));
		
	kakao.maps.event.addListener(map, 'idle', () => {
	// 줌 레벨이 일정 수준(예: 4이하)으로 낮을 때만 CCTV 표시 (너무 많으면 느려짐)
        if (map.getLevel() <= 4) {
            updateCctvMarkers(map);
        } else {
            cctvMarkers.forEach(m => m.setMap(null)); // 멀리서 볼 땐 끄기
        }
    });
}

let cctvMarkers = []; // 기존 마커 관리를 위한 배열

function updateCctvMarkers(map) {
    const bounds = map.getBounds();
    const sw = bounds.getSouthWest();
    const ne = bounds.getNorthEast();

    fetch(`/api/cctv?minLat=${sw.getLat()}&maxLat=${ne.getLat()}&minLng=${sw.getLng()}&maxLng=${ne.getLng()}`)
        .then(res => {
            if (!res.ok) throw new Error('서버 응답 에러');
            return res.json();
        })
        .then(data => {
            // 기존 마커 제거
            cctvMarkers.forEach(m => m.setMap(null));
            cctvMarkers = [];

            // ✅ 데이터가 배열인지 반드시 확인
            if (!Array.isArray(data)) {
                console.warn("CCTV 데이터 형식이 배열이 아닙니다:", data);
                return;
            }

			data.forEach(cctv => {
			    // 1. 마커 생성
			    const marker = new kakao.maps.Marker({
			        position: new kakao.maps.LatLng(cctv.latitude, cctv.longitude),
			        image: new kakao.maps.MarkerImage(
			            'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png',
			            new kakao.maps.Size(20, 30)
			        )
			    });

			    // 2. 인포윈도우 생성 (호버 시 나타날 내용)
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
			        disableAutoPan: true // 마커 호버할 때 지도가 이동하지 않게 설정
			    });

			    // 3. 이벤트 리스너 등록 (마우스 오버/아웃)
			    kakao.maps.event.addListener(marker, 'mouseover', function() {
			        infowindow.open(map, marker);
			    });

			    kakao.maps.event.addListener(marker, 'mouseout', function() {
			        infowindow.close();
			    });

			    marker.setMap(map);
			    cctvMarkers.push(marker);
			});
        })
        .catch(err => {
            console.error("CCTV 로딩 중 에러 발생:", err);
        });
}

// [함수] LH 마커 및 오버레이 표시
function displayLhMarker(notice, map) {
    const position = new kakao.maps.LatLng(notice.latitude, notice.longitude);

    const marker = new kakao.maps.Marker({
        map: map,
        position: position
    });

    // 마커 위에 둥둥 떠있는 유형 텍스트
    const content = `
        <div style="background: white; border: 1px solid #28a745; padding: 2px 6px; 
                    font-size: 11px; font-weight: bold; color: #28a745;
                    border-radius: 12px; transform: translateY(-40px); white-space: nowrap;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1); pointer-events: none;">
            ${notice.aisTpCdNm}
        </div>`;
    
    new kakao.maps.CustomOverlay({
        position: position,
        content: content,
        map: map
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
        </div>`;

    const infowindow = new kakao.maps.InfoWindow({
        content: iwContent,
        removable: true
    });

    kakao.maps.event.addListener(marker, 'click', function() {
        infowindow.open(map, marker);
    });
}


function drawSafePolyline(path, map) {
    try {
        const rawCoords = JSON.parse(path.pathCoordinates); 
        const linePath = rawCoords.map(p => new kakao.maps.LatLng(p[1], p[0]));

        const polyline = new kakao.maps.Polyline({
            path: linePath,
            strokeWeight: 6,
            strokeColor: '#2ECC71',
            strokeOpacity: 0.7,
            strokeStyle: 'solid'
        });

        polyline.setMap(map);

        // --- 1. 주소 정보 조립 (있는 데이터만 합치기) ---
        // sigungu, bjdName, detailLocation 순서
        const addressParts = [path.sigungu, path.bjdName, path.detailLocation];
        const fullAddr = addressParts.filter(part => part && part !== 'null' && part.trim() !== '').join(' ');

        // --- 2. 호버 시 나타날 툴팁 생성 ---
        const tooltipContent = `
            <div class="safe-tooltip" style="background: rgba(0, 0, 0, 0.85); color: white; padding: 10px 15px; border-radius: 10px; font-size: 12px; pointer-events: none; z-index: 1000; min-width: 180px;">
                <div style="font-weight: bold; margin-bottom: 6px; color: #2ECC71; border-bottom: 1px solid #444; padding-bottom: 4px;">
                    🛡️ 안심귀갓길 정보
                </div>
                ${fullAddr ? `<div style="margin-bottom: 8px; color: #ddd; font-size: 11px;">📍 ${fullAddr}</div>` : ''}
                <div style="display: flex; justify-content: space-between; gap: 10px;">
                    <span>🚨 벨: <b>${path.bellCount || 0}</b></span>
                    <span>📷 CCTV: <b>${path.cctvCount || 0}</b></span>
                    <span>💡 보안등: <b>${path.lampCount || 0}</b></span>
                </div>
            </div>`;

        const tooltip = new kakao.maps.CustomOverlay({
            content: tooltipContent,
            xAnchor: 0.5, 
            yAnchor: 1.3  
        });


        // 마우스 올렸을 때
        kakao.maps.event.addListener(polyline, 'mouseover', function(mouseEvent) {
            polyline.setOptions({ strokeOpacity: 1.0, strokeWeight: 8, strokeColor: '#27AE60' });
            tooltip.setPosition(mouseEvent.latLng);
            tooltip.setMap(map);
        });

        // 마우스 움직일 때 (커서 따라다니기)
        kakao.maps.event.addListener(polyline, 'mousemove', function(mouseEvent) {
            tooltip.setPosition(mouseEvent.latLng);
        });

        // 마우스 나갔을 때
        kakao.maps.event.addListener(polyline, 'mouseout', function() {
            polyline.setOptions({ strokeOpacity: 0.7, strokeWeight: 6, strokeColor: '#2ECC71' });
            tooltip.setMap(null);
        });

    } catch (e) {
        console.error("안심귀갓길 툴팁 렌더링 에러:", e);
    }
}