document.addEventListener("DOMContentLoaded", () => {
  // 현재 경로 기반 active 처리
  const path = location.pathname;
  document.querySelectorAll(".admin-nav a").forEach(a => {
    const href = a.getAttribute("href");
    if (href && path === href) a.classList.add("active");
  });

  // 검색 input(일단 UI만): Enter 누르면 q 파라미터로 이동
  document.querySelectorAll("[data-admin-search]").forEach(input => {
    input.addEventListener("keydown", (e) => {
      if (e.key !== "Enter") return;
      e.preventDefault();

      const q = input.value.trim();
      const url = new URL(location.href);
      if (q) url.searchParams.set("q", q);
      else url.searchParams.delete("q");
      url.searchParams.set("page", "0");
      location.href = url.toString();
    });
  });
});