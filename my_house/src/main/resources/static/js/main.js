// /static/js/main.js

// 0) 전역 변수 (필터 및 마커 관리용)
let cctvMarkers = [];
let cctvInfoWindows = [];
let noticeMarkers = [];
let safePolylines = [];
const filterStatus = { notice: true, safePath: true, cctv: true };

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
            }
        });
    };

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
        const recBtn = e.target.closest(".panel-recenter");
        if (recBtn) {
            const lat = parseFloat(recBtn.getAttribute("data-lat"));
            const lng = parseFloat(recBtn.getAttribute("data-lng"));
            const center = new kakao.maps.LatLng(lat, lng);
            window.__MAIN_MAP__.setCenter(center);
            window.__MAIN_MAP__.setLevel(3);
            window.__MAIN_MARKER__.setPosition(center);
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