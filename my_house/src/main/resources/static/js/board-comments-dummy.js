// /static/js/board-comments-dummy.js
(() => {
  const listEl = document.getElementById("commentList");
  const emptyEl = document.getElementById("commentEmpty");
  const countEl = document.getElementById("commentCount");
  const formEl = document.getElementById("commentForm");
  const inputEl = document.getElementById("commentInput");

  if (!listEl || !formEl || !inputEl) return;

  const postId = document.querySelector("[data-post-id]")?.dataset?.postId || "0";
  const storageKey = `board_comments_dummy_${postId}`;

  const seed = [
    { id: 1, writer: "관리자", content: "공지 확인했습니다!", createdAt: "2026-06-21 10:12" },
    { id: 2, writer: "익명", content: "점검 시간 참고할게요.", createdAt: "2026-06-21 11:03" }
  ];

  function escapeHtml(str) {
    return (str ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }

  function load() {
    const raw = localStorage.getItem(storageKey);
    if (!raw) {
      localStorage.setItem(storageKey, JSON.stringify(seed));
      return [...seed];
    }
    try { return JSON.parse(raw) ?? []; } catch { return []; }
  }

  function save(items) {
    localStorage.setItem(storageKey, JSON.stringify(items));
  }

  function nowStr() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  function render(items) {
    listEl.innerHTML = "";
    countEl.textContent = String(items.length);
    emptyEl.style.display = items.length === 0 ? "block" : "none";

    for (const c of items) {
      const li = document.createElement("li");
      li.className = "comment-item";
      li.dataset.id = String(c.id);

      li.innerHTML = `
        <div class="comment-meta">
          <span><strong>${escapeHtml(c.writer)}</strong> · ${escapeHtml(c.createdAt)}</span>
          <div class="comment-actions">
		    <button class="comment-btn comment-btn--edit comment-edit-open" type="button" data-id="${c.id}">수정</button>
		    <button class="comment-btn comment-btn--danger comment-del" type="button" data-id="${c.id}">삭제</button>
          </div>
        </div>

        <div class="comment-body" data-role="body">${escapeHtml(c.content)}</div>

        <div class="comment-edit" data-role="edit">
          <input type="text" maxlength="200" value="${escapeHtml(c.content)}" />
		  <button class="comment-btn comment-btn--primary comment-edit-save" type="button" data-id="${c.id}">저장</button>
		  <button class="comment-btn comment-btn--ghost comment-edit-cancel" type="button" data-id="${c.id}">취소</button>
        </div>
      `;
      listEl.appendChild(li);
    }
  }

  function closeAllEdits() {
    listEl.querySelectorAll('[data-role="edit"]').forEach(el => el.classList.remove("is-open"));
  }

  let comments = load();
  render(comments);

  // 등록
  formEl.addEventListener("submit", (e) => {
    e.preventDefault();
    const text = inputEl.value.trim();
    if (!text) return;

    const nextId = (comments[0]?.id ?? 0) + 1;
    comments = [{ id: nextId, writer: "나", content: text, createdAt: nowStr() }, ...comments];
    save(comments);
    inputEl.value = "";
    closeAllEdits();
    render(comments);
  });

  // 수정/삭제 이벤트 위임
  listEl.addEventListener("click", (e) => {
    const delBtn = e.target.closest(".comment-del");
    const openBtn = e.target.closest(".comment-edit-open");
    const saveBtn = e.target.closest(".comment-edit-save");
    const cancelBtn = e.target.closest(".comment-edit-cancel");

    // 삭제
    if (delBtn) {
      const id = Number(delBtn.dataset.id);
      if (!confirm("댓글을 삭제할까요?")) return;

      comments = comments.filter(c => c.id !== id);
      save(comments);
      closeAllEdits();
      render(comments);
      return;
    }

    // 수정 열기
    if (openBtn) {
      const id = Number(openBtn.dataset.id);

      // 다른 편집 닫고, 해당 댓글만 열기
      closeAllEdits();
      const item = listEl.querySelector(`.comment-item[data-id="${id}"]`);
      if (!item) return;
      const editBox = item.querySelector('[data-role="edit"]');
      const input = editBox?.querySelector("input");
      if (!editBox || !input) return;

      editBox.classList.add("is-open");
      input.focus();
      input.setSelectionRange(input.value.length, input.value.length);
      return;
    }

    // 수정 저장
    if (saveBtn) {
      const id = Number(saveBtn.dataset.id);
      const item = listEl.querySelector(`.comment-item[data-id="${id}"]`);
      if (!item) return;

      const editBox = item.querySelector('[data-role="edit"]');
      const input = editBox?.querySelector("input");
      const newText = (input?.value ?? "").trim();
      if (!newText) return;

      comments = comments.map(c => (c.id === id ? { ...c, content: newText } : c));
      save(comments);
      closeAllEdits();
      render(comments);
      return;
    }

    // 수정 취소
    if (cancelBtn) {
      closeAllEdits();
      return;
    }
  });

  // Enter로 저장 / Esc로 취소 (편집 input에서)
  listEl.addEventListener("keydown", (e) => {
    const input = e.target.closest('.comment-edit input');
    if (!input) return;

    const item = e.target.closest(".comment-item");
    const id = Number(item?.dataset?.id);

    if (e.key === "Enter") {
      e.preventDefault();
      item?.querySelector(".comment-edit-save")?.click();
    }
    if (e.key === "Escape") {
      e.preventDefault();
      closeAllEdits();
    }
  });
})();