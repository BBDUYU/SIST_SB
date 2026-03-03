document.addEventListener('DOMContentLoaded', () => {
  const tabs = document.querySelectorAll('.board-tab');
  const searchInput = document.querySelector('#boardSearch');
  const rows = document.querySelectorAll('#boardTableBody tr');

  tabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      const category = tab.dataset.category;
      window.location.href = `/board?category=${category}`;
    });
  });

  searchInput?.addEventListener('input', (event) => {
    const keyword = event.target.value.trim().toLowerCase();

    rows.forEach((row) => {
      const text = row.textContent.toLowerCase();
      row.style.display = text.includes(keyword) ? '' : 'none';
    });
  });
});