// /static/js/main.js

// 0) 전역 변수 (필터 및 마커 관리용)
let cctvMarkers = [];
let cctvInfoWindows = [];
let noticeMarkers = [];
let safePolylines = [];
const filterStatus = { notice: true, safePath: true, cctv: true };
window.closeRvModal = function() {
    const modal = document.getElementById('rvModal');
    if (modal) modal.style.display = 'none';
};
document.addEventListener("DOMContentLoaded", () => {
    const $ = (sel) => document.querySelector(sel);

    // 1) 지도 생성
    const mapContainer = document.getElementById("map");
    if (mapContainer && window.kakao?.maps) {
        const map = new kakao.maps.Map(mapContainer, {
            center: new kakao.maps.LatLng(37.5665, 126.9780),
            level: 5,
        });

        const mainMarker = new kakao.maps.Marker({
            position: new kakao.maps.LatLng(37.5665, 126.9780),
        });
        mainMarker.setMap(map);

        window.__MAIN_MAP__ = map;
        window.__MAIN_MARKER__ = mainMarker;

        // 초기화 함수들 실행
        initAdditionalLayers(map);
        initComplexBoundsSync(map);
    }

    // 2) 패널 SPA 로직
    const panel = document.getElementById("panelContent");
    if (!panel) return;
    let listHTML = panel.innerHTML;

    function animateSwap(nextHTML, afterSwap) {
        panel.classList.remove("panel-enter");
        panel.classList.add("panel-anim", "panel-leave");
        setTimeout(() => {
            panel.innerHTML = nextHTML;
            panel.classList.remove("panel-leave");
            panel.classList.add("panel-enter");
            requestAnimationFrame(() => panel.classList.remove("panel-enter"));
            if (typeof afterSwap === "function") afterSwap();
        }, 210);
    }

    // 상세 열기 (인프라 통계 포함 버전)
    window.openDetail = async function(propertyKey) {
        const res = await fetch(`/listing/${propertyKey}/panel`, {
            headers: { "X-Requested-With": "fetch" },
        });
        if (!res.ok) return;
        const html = await res.text();
        animateSwap(html, () => {
            const btn = document.querySelector(".panel-recenter");
            if (btn) {
                const lat = parseFloat(btn.getAttribute("data-lat"));
                const lng = parseFloat(btn.getAttribute("data-lng"));
                updateInfraStats(lat, lng);
				initRoadview('roadview', lat, lng);
				
            }
        });
    };

	//로드뷰
	// ✅ 1. 로드뷰 초기화 (상세페이지 내 작은 화면)
	function initRoadview(containerId, lat, lng) {
	    const container = document.getElementById(containerId);
	    if (!container) return;

	    const rv = new kakao.maps.Roadview(container);
	    const rvClient = new kakao.maps.RoadviewClient();
	    const position = new kakao.maps.LatLng(lat, lng);

	    rvClient.getNearestPanoId(position, 50, (panoId) => {
	        if (panoId) rv.setPanoId(panoId, position);
	        else container.innerHTML = '<div class="text-muted p-5 text-center">로드뷰 없음</div>';
	    });
	}

	window.openRvModal = function(lat, lng) {
	        const modal = document.getElementById('rvModal');
	        const content = document.getElementById('rvFullContent');
	        if (!modal || !content) return;

	        modal.style.display = 'block';
	        content.innerHTML = ''; 

	        const rv = new kakao.maps.Roadview(content);
	        const rvClient = new kakao.maps.RoadviewClient();
	        const position = new kakao.maps.LatLng(lat, lng);

	        rvClient.getNearestPanoId(position, 50, (panoId) => {
	            if (panoId) {
	                rv.setPanoId(panoId, position);
	            } else {
	                content.innerHTML = '<div style="color:white; display:flex; align-items:center; justify-content:center; height:100%;">이 지역은 로드뷰를 지원하지 않습니다.</div>';
	            }
	        });
	    };

	    // ✅ 3. 클릭 이벤트 위임 수정 (중복 리스너 제거 및 정리)
	    document.addEventListener("click", (e) => {
	        // 크게보기 버튼
	        const expandBtn = e.target.closest(".btn-rv-expand");
	        if (expandBtn) {
	            const btnRecenter = document.querySelector(".panel-recenter");
	            if (btnRecenter) {
	                const lat = btnRecenter.getAttribute("data-lat");
	                const lng = btnRecenter.getAttribute("data-lng");
	                window.openRvModal(lat, lng);
	            }
	            return;
	        }

	        // 배경 클릭 시 닫기 기능 추가
	        if (e.target.id === 'rvModal') {
	            window.closeRvModal();
	        }
	    });


	
    function backToList() {
        animateSwap(listHTML, () => {
            if (window.__MAIN_MAP__) {
                listHTML = panel.innerHTML;
                window.__UPDATE_COMPLEX_BY_BOUNDS__?.();
            }
        });
    }

    // 3) 클릭 이벤트 위임
    document.addEventListener("click", (e) => {
        // 하트 토글
        const heartBtn = e.target.closest(".heart-btn, #btnHeart.iconBtn");
        if (heartBtn) {
            e.preventDefault();
            if (heartBtn.id === "btnHeart") {
                heartBtn.classList.toggle("hearted");
                const path = heartBtn.querySelector("svg path");
                const isOn = heartBtn.classList.contains("hearted");
                path.setAttribute("fill", isOn ? "#DC2626" : "none");
                path.setAttribute("stroke", isOn ? "#DC2626" : "#0F172A");
            } else {
                const icon = heartBtn.querySelector("i");
                icon.classList.toggle("bi-heart");
                icon.classList.toggle("bi-heart-fill");
                icon.style.color = icon.classList.contains("bi-heart-fill") ? "#e11d48" : "";
            }
            return;
        }

        // 카드 클릭
        const item = e.target.closest(".house-item");
        if (item) {
            const key = item.getAttribute("data-key");
            if (key) openDetail(key);
            return;
        }

        // 닫기
        if (e.target.closest(".panel-close")) return backToList();

        // 필터 칩
        const chip = e.target.closest(".filter-chip");
        if (chip) {
            const type = chip.getAttribute("data-filter");
            const active = chip.classList.toggle("active");
            filterStatus[type] = active;
            applyFilter(type, active);
            return;
        }

        // 위치 재중심
		// 위치 재중심 버튼 클릭 시
		const recBtn = e.target.closest(".panel-recenter");
		if (recBtn) {
		    const lat = parseFloat(recBtn.getAttribute("data-lat"));
		    const lng = parseFloat(recBtn.getAttribute("data-lng"));
		    
		    if (isNaN(lat) || isNaN(lng)) {
		        console.error("좌표 값이 유효하지 않습니다.");
		        return;
		    }

		    const center = new kakao.maps.LatLng(lat, lng);
		    const map = window.__MAIN_MAP__;
		    const marker = window.__MAIN_MARKER__;

		    if (map) {
		        map.setLevel(3); 
		        
		        setTimeout(() => {
		            map.panTo(center); 
		            
		            if (marker) {
		                marker.setPosition(center);
		                marker.setMap(map); // 혹시 꺼져있을 경우를 대비해 다시 표시
		            }

		            const focusOverlay = new kakao.maps.CustomOverlay({
		                position: center,
		                content: '<div class="focus-ring"></div>',
		                zIndex: 4 // 마커보다 위에 오도록 조정
		            });

		            focusOverlay.setMap(map);
		            setTimeout(() => focusOverlay.setMap(null), 2000);
		        }, 100);
		    }
		}
    });
});

// --- 기능 함수들 (독립 선언) ---

function initComplexBoundsSync(map) {
    const complexMarkerImage = new kakao.maps.MarkerImage("/image/pin.png", new kakao.maps.Size(48, 48), { offset: new kakao.maps.Point(24, 48) });
    let complexMarkers = [];
    let debounceTimer = null;

    const update = async () => {
        if (map.getLevel() > 5) {
            complexMarkers.forEach(m => m.setMap(null));
            const el = document.getElementById("complexList");
            if (el) el.innerHTML = `<div class="text-muted p-3">지도를 확대해주세요.</div>`;
            return;
        }
        const b = map.getBounds();
        const url = `/api/complex/in-bounds?swLat=${b.getSouthWest().getLat()}&swLng=${b.getSouthWest().getLng()}&neLat=${b.getNorthEast().getLat()}&neLng=${b.getNorthEast().getLng()}`;
        const res = await fetch(url);
        const data = await res.json();

        // 마커 초기화 및 생성
        complexMarkers.forEach(m => m.setMap(null));
        complexMarkers = data.map(it => {
            const m = new kakao.maps.Marker({ map, position: new kakao.maps.LatLng(it.latitude, it.longitude), image: complexMarkerImage });
            kakao.maps.event.addListener(m, 'click', () => openDetail(String(it.cid)));
            return m;
        });

        // 리스트 UI 생성
        const listEl = document.getElementById("complexList") || (function() {
            const body = document.querySelector(".panel-body");
            if (!body) return null;
            const newEl = document.createElement("div"); newEl.id = "complexList";
            body.appendChild(newEl); return newEl;
        })();
        if (listEl) {
            listEl.innerHTML = data.map(it => `
                <div class="card house-card mb-3 house-item" data-key="${it.cid}">
                    <div class="card-body d-flex gap-3 align-items-center">
                        <div class="thumb"></div>
                        <div class="flex-grow-1">
                            <div class="fw-semibold">${it.title || ""}</div>
                            <div class="small text-muted">${it.address || ""}</div>
                        </div>
                        <a href="#" class="heart-btn"><i class="bi bi-heart"></i></a>
                    </div>
                </div>`).join("");
        }
    };

    kakao.maps.event.addListener(map, "idle", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(update, 200);
    });
    window.__UPDATE_COMPLEX_BY_BOUNDS__ = update;
    update();
}

function initAdditionalLayers(map) {
    // LH 공고
    fetch('/api/notices').then(res => res.json()).then(data => {
        data.forEach(n => {
            if (!n.latitude) return;
            const pos = new kakao.maps.LatLng(n.latitude, n.longitude);
            const marker = new kakao.maps.Marker({ map, position: pos });
            const overlay = new kakao.maps.CustomOverlay({
                position: pos,
                content: `<div style="background:white; border:1px solid #28a745; padding:2px 6px; border-radius:12px; font-size:11px; color:#28a745; transform:translateY(-40px); font-weight:bold;">${n.aisTpCdNm}</div>`,
                map: map
            });
            noticeMarkers.push({ marker, overlay });
            kakao.maps.event.addListener(marker, 'click', () => {
                new kakao.maps.InfoWindow({ content: `<div style="padding:15px; width:250px;"><b>${n.panNm}</b><br>마감: ${n.clsgDt}<br><a href="${n.dtlUrl}" target="_blank">상세보기</a></div>`, removable: true }).open(map, marker);
            });
        });
    });

    // 안심귀갓길
    fetch('/api/safe-paths').then(res => res.json()).then(paths => {
        paths.forEach(p => {
            const poly = new kakao.maps.Polyline({
                path: JSON.parse(p.pathCoordinates).map(c => new kakao.maps.LatLng(c[1], c[0])),
                strokeWeight: 6, strokeColor: '#2ECC71', strokeOpacity: 0.7
            });
            poly.setMap(map);
            safePolylines.push(poly);
        });
    });

    // CCTV (줌 레벨 연동)
    kakao.maps.event.addListener(map, 'idle', () => {
        if (filterStatus.cctv && map.getLevel() <= 2) updateCctvMarkers(map);
        else { cctvMarkers.forEach(m => m.setMap(null)); cctvInfoWindows.forEach(iw => iw.close()); }
    });
}

function updateCctvMarkers(map) {
    const b = map.getBounds();
    fetch(`/api/cctv?minLat=${b.getSouthWest().getLat()}&maxLat=${b.getNorthEast().getLat()}&minLng=${b.getSouthWest().getLng()}&maxLng=${b.getNorthEast().getLng()}`)
        .then(res => res.json()).then(data => {
            cctvMarkers.forEach(m => m.setMap(null)); cctvMarkers = [];
            cctvInfoWindows.forEach(iw => iw.close()); cctvInfoWindows = [];
            data.forEach(c => {
                const m = new kakao.maps.Marker({
                    position: new kakao.maps.LatLng(c.latitude, c.longitude),
                    image: new kakao.maps.MarkerImage('https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png', new kakao.maps.Size(20, 30))
                });
                const iw = new kakao.maps.InfoWindow({ content: `<div style="padding:10px;">📷 CCTV: ${c.purposeDesc}</div>` });
                kakao.maps.event.addListener(m, 'mouseover', () => iw.open(map, m));
                kakao.maps.event.addListener(m, 'mouseout', () => iw.close());
                m.setMap(map);
                cctvMarkers.push(m);
                cctvInfoWindows.push(iw);
            });
        });
}

function applyFilter(type, show) {
    const map = window.__MAIN_MAP__;
    if (!map) return;
    const target = show ? map : null;
    if (type === 'notice') noticeMarkers.forEach(n => { n.marker.setMap(target); n.overlay.setMap(target); });
    else if (type === 'safePath') safePolylines.forEach(l => l.setMap(target));
    else if (type === 'cctv') { 
        if (!show) { cctvMarkers.forEach(m => m.setMap(null)); cctvInfoWindows.forEach(iw => iw.close()); } 
        else updateCctvMarkers(map);
    }
}

async function updateInfraStats(lat, lng) {
    const ps = new kakao.maps.services.Places();
    const getCount = (code, radius) => new Promise(res => {
        ps.categorySearch(code, (data, status, pagination) => res(status === kakao.maps.services.Status.OK ? pagination.totalCount : 0), { location: new kakao.maps.LatLng(lat, lng), radius });
    });
    const [s, c, h, m] = await Promise.all([getCount('PK6', 1000), getCount('CS2', 300), getCount('HP8', 1000), getCount('MT1', 1000)]);
    if(document.getElementById('subway-count')) document.getElementById('subway-count').innerText = s;
    if(document.getElementById('cvs-count')) document.getElementById('cvs-count').innerText = c;
    if(document.getElementById('hospital-count')) document.getElementById('hospital-count').innerText = h;
    if(document.getElementById('mart-count')) document.getElementById('mart-count').innerText = m;
}

// 인프라
async function updateInfraStats(lat, lng) {
    // 카카오 로컬 서비스 객체 생성
    const ps = new kakao.maps.services.Places();
    const location = new kakao.maps.LatLng(lat, lng);

    // 공통 검색 함수 (Promise화)
    const getCount = (categoryCode, radius) => new Promise(resolve => {
        ps.categorySearch(categoryCode, (data, status, pagination) => {
            if (status === kakao.maps.services.Status.OK) {
                // 검색된 전체 개수 반환
                resolve(pagination.totalCount);
            } else {
                resolve(0);
            }
        }, { location, radius });
    });

    try {
        // 4가지 카테고리 동시 조회
        // SW8: 지하철역, CS2: 편의점, HP8: 병원, MT1: 대형마트
        const [subway, cvs, hospital, mart] = await Promise.all([
            getCount('SW8', 1000), // 지하철 (1km)
            getCount('CS2', 300),  // 편의점 (300m)
            getCount('HP8', 1000), // 병원 (1km)
            getCount('MT1', 1000)  // 마트 (1km)
        ]);

        // HTML 요소 업데이트 및 상태(Pill) 변경
        updateInfraUI('subway-count', subway, 1);     // 1개만 있어도 Good
        updateInfraUI('cvs-count', cvs, 3);        // 3개 이상 Good
        updateInfraUI('hospital-count', hospital, 2);  // 2개 이상 Good
        updateInfraUI('mart-count', mart, 1);          // 1개만 있어도 Good

    } catch (e) {
        console.error("인프라 정보 로드 실패:", e);
    }
}

// UI 업데이트 보조 함수
function updateInfraUI(id, count, threshold) {
    const el = document.getElementById(id);
    if (!el) return;

    el.innerText = count;
    
    // 부모 .pill 태그의 클래스 변경 (상태 표시)
    const pill = el.closest('.pill');
    if (pill) {
        if (count >= threshold) {
            pill.className = 'pill good';
            pill.innerText = '좋음';
        } else if (count > 0) {
            pill.className = 'pill';
            pill.innerText = '보통';
        } else {
            pill.className = 'pill warn';
            pill.innerText = '아쉬움';
        }
        // 숫자를 다시 넣어줌 (텍스트가 '좋음'으로 덮어씌워지므로)
        const span = document.createElement('span');
        span.id = id;
        span.innerText = count + '개';
        pill.prepend(span); 
        // 최종 형태 예: "3개 좋음"
    }
}

