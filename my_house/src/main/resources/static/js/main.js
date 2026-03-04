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

function getCsrf() {
  const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
  return { token, header };
}

document.addEventListener("DOMContentLoaded", () => {
    const $ = (sel) => document.querySelector(sel);

    // 1) 지도 생성
    const mapContainer = document.getElementById("map");
    if (mapContainer && window.kakao?.maps) {
        const map = new kakao.maps.Map(mapContainer, {
            center: new kakao.maps.LatLng(37.5665, 126.9780),
            level: 5,
        });

       


        window.__MAIN_MAP__ = map;

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
        animateSwap(html, async () => {
            const btn = document.querySelector(".panel-recenter");
            if (btn) {
                const lat = parseFloat(btn.getAttribute("data-lat"));
                const lng = parseFloat(btn.getAttribute("data-lng"));
                updateInfraStats(lat, lng);
				initRoadview('roadview', lat, lng);
				
            }
			

			// 상세 하트 상태 유지
			const hb = document.getElementById("btnHeart");
			if (hb) {
			    const cid = Number(hb.dataset.cid);
			    const r = await fetch("/mypage/wishlist/ids", { credentials: "same-origin" });
			    const ids = new Set((await r.json()).map(Number));

			    const isOn = ids.has(cid);
			    hb.classList.toggle("hearted", isOn);

			    const path = hb.querySelector("svg path");
			    if (path) {
			        path.setAttribute("fill", isOn ? "#DC2626" : "none");
			        path.setAttribute("stroke", isOn ? "#DC2626" : "#0F172A");
			    }
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
    document.addEventListener("click", async (e) => {
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
			// DB 저장 토글
			// ✅ 여기부터 수정 (DB 저장 토글)
			try {
				const item = heartBtn.closest(".house-item");
				const cid =
				  Number(heartBtn.dataset.cid) ||   // 상세페이지 하트
				  (item ? Number(item.getAttribute("data-key")) : null); // 목록 하트
			  if (!cid) throw new Error("cid 없음");

			  const { token, header } = getCsrf();

			  const res = await fetch(`/mypage/wishlist/toggle?cid=${cid}`, {
			    method: "POST",
			    headers: {
			      ...(token && header ? { [header]: token } : {}),
			      "X-Requested-With": "XMLHttpRequest",
			    },
			    credentials: "same-origin"
			  });

			  const json = await res.json();

			  if (json.ok) {
			    console.log("wishlist 상태:", json.hearted ? "추가됨" : "삭제됨");
			  } else {
			    alert("로그인이 필요합니다.");
			  }

			} catch (err) {
			  console.error("wishlist toggle error:", err);
			}
			
            return;
        }
		
		// 위시리스트 삭제 버튼 (.btn-icon.btn-remove)
		const removeBtn = e.target.closest(".btn-icon.btn-remove");
		if (removeBtn) {
		  e.preventDefault();

		  const wid = removeBtn.dataset.id; // 버튼에 data-id 필요
		  if (!wid) {
		    console.error("wishlist id 없음 (data-id 확인)");
		    return;
		  }

		  const { token, header } = getCsrf();

		  const res = await fetch(`/mypage/wishlist/remove?id=${wid}`, {
		    method: "POST",
		    headers: {
		      ...(token && header ? { [header]: token } : {}),
		      "X-Requested-With": "XMLHttpRequest",
		    },
		    credentials: "same-origin",
		  });

		  if (!res.ok) {
		    console.error("wishlist remove failed:", res.status);
		    return;
		  }

		  // 화면에서 카드 제거 (위시리스트 페이지 카드 wrapper 클래스에 맞춰 조정)
		  removeBtn.closest(".wishlist-card")?.remove();
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
		
		const res2 = await fetch("/mypage/wishlist/ids", { credentials: "same-origin" });
		const wishedIds = new Set((await res2.json()).map(Number));

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
		    listEl.innerHTML = data.map(it => {
		        const isWished = wishedIds.has(Number(it.cid));
		        return `
		        <div class="card house-card mb-3 house-item" data-key="${it.cid}">
		            <div class="card-body d-flex gap-3 align-items-center">
		                <div class="thumb"></div>
		                <div class="flex-grow-1">
		                    <div class="fw-semibold">${it.title || ""}</div>
		                    <div class="small text-muted">${it.address || ""}</div>
		                </div>
		                <a href="#" class="heart-btn">
		                    <i class="bi ${isWished ? "bi-heart-fill" : "bi-heart"}"
		                       style="${isWished ? "color:#e11d48" : ""}"></i>
		                </a>
		            </div>
		        </div>`;
		    }).join("");
		}
    };

    kakao.maps.event.addListener(map, "idle", () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(update, 200);
    });
    window.__UPDATE_COMPLEX_BY_BOUNDS__ = update;
    update();
}


function lazyLoadRoadview() {
    const containers = document.querySelectorAll('.rv-thumb-container');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const el = entry.target;
                const lat = el.getAttribute('data-lat');
                const lng = el.getAttribute('data-lng');
                
                // 화면에 보일 때만 로드뷰 생성
                const rv = new kakao.maps.Roadview(el);
                const rvClient = new kakao.maps.RoadviewClient();
                const pos = new kakao.maps.LatLng(lat, lng);
                
				rvClient.getNearestPanoId(pos, 50, (panoId) => {
				    if (panoId) {
				        rv.setPanoId(panoId, pos);

				        // ✅ 핵심: 로드뷰의 시야를 넓게(줌 아웃) 설정
				        // 숫자가 작을수록(음수일수록) 더 멀리, 더 넓게 보입니다.
				        rv.setViewpoint({
				            pan: 0,
				            tilt: 0,
				            zoom: -2 // 기본값은 0, -2 정도로 낮추면 훨씬 넓게 보입니다.
				        });

				        el.style.pointerEvents = "none";
				    } else {
				        el.innerHTML = '<i class="bi bi-camera-video-off text-muted"></i>';
				    }
				});
                
                // 한 번 로드되면 감시 중단
                observer.unobserve(el);
            }
        });
    }, { threshold: 0.1 });

    containers.forEach(c => observer.observe(c));
}

document.addEventListener("DOMContentLoaded", function () {
    // 위에서 생성된 window.__MAIN_MAP__이 잡힐 때까지 잠시 대기하거나 바로 실행
    const checkMap = setInterval(() => {
        if (window.__MAIN_MAP__) {
            clearInterval(checkMap);
            initAdditionalLayers(window.__MAIN_MAP__);
        }
    }, 100);
	
	// ✅ /main?cid=123 으로 들어오면 상세 패널 자동 오픈 (+ focus=review면 리뷰 위치로)
	const params = new URLSearchParams(window.location.search);
	const cid = params.get("cid");
	const focus = params.get("focus");

	if (cid && window.openDetail) {
	  window.openDetail(cid);

	  if (focus === "review") {
	    // 패널이 들어오고 리뷰 모듈이 붙을 시간 조금 대기
	    setTimeout(() => {
	      const reviewEl =
	        document.getElementById("reviewForm") ||
	        document.querySelector(".reviewForm") ||
	        document.querySelector("#reviewList") ||
	        document.querySelector(".reviewList");

	      reviewEl?.scrollIntoView({ behavior: "smooth", block: "start" });
	    }, 600);
	  }
	}
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
	        // 필터가 켜져있고, 지도가 충분히 확대(레벨 2 이하)되었을 때만 CCTV 표시
	        if (filterStatus.cctv && map.getLevel() <= 3) { 
	            updateCctvMarkers(map);
	        } else {
	            // 멀어지면 마커 정리
	            cctvMarkers.forEach(m => m.setMap(null));
				cctvInfoWindows.forEach(iw => {
				    if (iw && iw.setMap) iw.setMap(null); 
				});
	        }
	    });
}

// [함수] LH 마커 및 오버레이 표시
// [함수] LH 마커 및 오버레이 표시
function displayLhMarker(notice, map) {
    const position = new kakao.maps.LatLng(notice.latitude, notice.longitude);

    // 1. 마커 생성
    const marker = new kakao.maps.Marker({
        map: map,
        position: position
    });

    // 2. 오버레이 생성 (변수명을 lhOverlay로 명확히 지정)
    const content = `
        <div style="background: white; border: 1px solid #28a745; padding: 2px 6px; 
                    font-size: 11px; font-weight: bold; color: #28a745;
                    border-radius: 12px; transform: translateY(-40px); white-space: nowrap;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1); pointer-events: none;">
            ${notice.aisTpCdNm}
        </div>`;
    
    const lhOverlay = new kakao.maps.CustomOverlay({
        position: position,
        content: content,
        map: map
    });

    // ✅ [핵심] 전역 배열에 저장 (변수명 lhOverlay 사용)
    noticeMarkers.push({ marker: marker, overlay: lhOverlay });

    // 3. 인포윈도우(클릭 시 상세정보) 설정
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
		safePolylines.push(polyline);
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

function updateCctvMarkers(map) {
    const b = map.getBounds();
    const url = `/api/cctv?minLat=${b.getSouthWest().getLat()}&maxLat=${b.getNorthEast().getLat()}&minLng=${b.getSouthWest().getLng()}&maxLng=${b.getNorthEast().getLng()}`;
    
    fetch(url)
        .then(res => res.json())
        .then(data => {
            // 기존 마커 및 오버레이 제거
            cctvMarkers.forEach(m => m.setMap(null));
            cctvMarkers = [];
            // InfoWindow 대신 Overlay를 관리하기 위해 기존 리스트 재활용 또는 정리
            cctvInfoWindows.forEach(iw => { if(iw.setMap) iw.setMap(null); });
            cctvInfoWindows = [];
            
            data.forEach(c => {
                const position = new kakao.maps.LatLng(c.latitude, c.longitude);
                
                // 1. 마커 생성
                const m = new kakao.maps.Marker({
                    position: position,
                    image: new kakao.maps.MarkerImage(
                        'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png', 
                        new kakao.maps.Size(20, 30)
                    )
                });

                // 2. 커스텀 오버레이 생성 (InfoWindow 대신!)
                // 하얀 박스가 생기지 않도록 완전히 커스텀 HTML만 사용합니다.
                const content = `
                    <div class="cctv-overlay" style="
                        padding:7px 12px; 
                        background: rgba(51, 65, 85, 0.95); 
                        color:#fff; 
                        border-radius:20px; 
                        font-size:12px; 
                        font-weight:500; 
                        box-shadow:0 4px 12px rgba(0,0,0,0.3); 
                        white-space:nowrap; 
                        pointer-events:none;
                        margin-bottom: 35px; /* 마커 위에 띄우기 */
                        position: relative;
                    ">
                        <i class="bi bi-camera-video-fill" style="color:#fbbf24; margin-right:4px;"></i>
                        CCTV: ${c.purposeDesc}
                        <div style="
                            position: absolute;
                            bottom: -5px;
                            left: 50%;
                            transform: translateX(-50%);
                            border-top: 6px solid rgba(51, 65, 85, 0.95);
                            border-left: 6px solid transparent;
                            border-right: 6px solid transparent;
                        "></div>
                    </div>`;

                const overlay = new kakao.maps.CustomOverlay({
                    content: content,
                    position: position,
					yAnchor: 1.2,
					clickable: false 
                });

                // 3. 마우스 이벤트: 오버레이 보이기/숨기기
                kakao.maps.event.addListener(m, 'mouseover', () => overlay.setMap(map));
                kakao.maps.event.addListener(m, 'mouseout', () => overlay.setMap(null));
                
                m.setMap(map);
                cctvMarkers.push(m);
                cctvInfoWindows.push(overlay); // 관리를 위해 배열에 저장
            });
        })
        .catch(err => console.error("CCTV 로딩 실패:", err));
}

function applyFilter(type, show) {
    const map = window.__MAIN_MAP__;
    if (!map) return;
    const target = show ? map : null;
    if (type === 'notice') noticeMarkers.forEach(n => { n.marker.setMap(target); n.overlay.setMap(target); });
    else if (type === 'safePath') safePolylines.forEach(l => l.setMap(target));
	else if (type === 'cctv') { 
	    if (!show) { 
	        cctvMarkers.forEach(m => m.setMap(null)); 
	        // CustomOverlay는 .close() 대신 .setMap(null)을 사용합니다.
	        cctvInfoWindows.forEach(iw => {
	            if (iw && iw.setMap) iw.setMap(null);
	        });
	    } 
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

// ✅ 마이페이지 리뷰 카드 클릭 -> 해당 매물 상세 + 리뷰 위치로 이동
document.addEventListener("click", (e) => {
  const card = e.target.closest(".review-card");
  if (!card) return;

  const cid = card.dataset.cid;
  if (!cid) return;

  location.href = `/main?cid=${cid}&focus=review`;
});