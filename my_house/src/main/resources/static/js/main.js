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