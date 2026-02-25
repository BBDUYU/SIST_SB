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

});

document.querySelector('.fab-noti')?.classList.add('has-noti');   // 점 켜기
// document.querySelector('.fab-noti')?.classList.remove('has-noti'); // 점 끄기