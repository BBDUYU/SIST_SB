(function () {
  // 첫 진입: 살짝 페이드 인
  document.body.classList.add('page-enter');
  requestAnimationFrame(() => {
    document.body.classList.remove('page-enter');
  });

  // 내부 링크 클릭: 페이드 아웃 후 이동
  document.addEventListener('click', (e) => {
    const a = e.target.closest('a');
    if (!a) return;

    const href = a.getAttribute('href');
    if (!href) return;

    // 새탭/다운로드/외부/앵커/JS 링크 등은 제외
    if (a.target === '_blank') return;
    if (a.hasAttribute('download')) return;
    if (href.startsWith('http')) return;
    if (href.startsWith('#')) return;
    if (href.startsWith('javascript:')) return;

    // 같은 origin 내 이동만 처리
    e.preventDefault();

    document.body.classList.add('page-leave');

    // CSS transition 끝나면 이동
    const go = () => (window.location.href = href);

    // transitionend가 안 잡히는 상황 대비 타임아웃도 같이
    const t = setTimeout(go, 220);

    document.body.addEventListener(
      'transitionend',
      () => {
        clearTimeout(t);
        go();
      },
      { once: true }
    );
  });
})();