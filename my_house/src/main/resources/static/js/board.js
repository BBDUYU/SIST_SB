document.addEventListener("DOMContentLoaded", () => {
  const tabs = document.querySelectorAll(".board-tab");
  const searchInput = document.querySelector("#boardSearch");

  const params = new URLSearchParams(window.location.search);
  const currentCategory = params.get("category") || "all";
  const currentQ = params.get("q") || "";

  if (searchInput) searchInput.value = currentQ;

  tabs.forEach((tab) => {
	tab.addEventListener("click", () => {
      const category = tab.dataset.category || "all";

      const next = new URLSearchParams(window.location.search);
      next.set("category", category);

      if (currentQ) next.set("q", currentQ);
      else next.delete("q");

      next.set("page", "0");

      window.location.href = `/board?${next.toString()}`;
    });
  });

  // 검색: Enter 누르면 서버 검색(q)으로 이동 + page=0 + category 유지
  searchInput?.addEventListener("keydown", (e) => {
    if (e.key !== "Enter") return;
    e.preventDefault();

	const keyword = searchInput.value.trim();

    const next = new URLSearchParams(window.location.search);
    next.set("category", currentCategory);

    if (keyword) next.set("q", keyword);
    else next.delete("q");

    next.set("page", "0");

    window.location.href = `/board?${next.toString()}`;
  });
});