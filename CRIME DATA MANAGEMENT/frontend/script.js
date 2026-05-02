// ================================================================
//  CRIME DATA MANAGEMENT — Frontend JavaScript
// ================================================================

const API = "http://localhost:8080/api/crimes";

// ── Tab Switching ────────────────────────────────────────────────
function switchTab(name) {
  document.querySelectorAll(".tab-panel").forEach(p => p.classList.remove("active"));
  document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));

  document.getElementById("tab-" + name).classList.add("active");

  const btns = document.querySelectorAll(".tab-btn");
  const map  = { add: 0, view: 1, update: 2, delete: 3 };
  btns[map[name]].classList.add("active");

  if (name === "view") loadCrimes();
}

// ── Toast Notification ───────────────────────────────────────────
function showToast(msg, type = "success") {
  const t = document.getElementById("toast");
  t.textContent = msg;
  t.className   = "show " + type;
  clearTimeout(t._timer);
  t._timer = setTimeout(() => { t.className = ""; }, 3200);
}

// ── Status Badge HTML ─────────────────────────────────────────────
function badgeFor(status) {
  if (status === "Open")               return `<span class="badge badge-open">${status}</span>`;
  if (status === "Under Investigation") return `<span class="badge badge-investigation">${status}</span>`;
  if (status === "Closed")             return `<span class="badge badge-closed">${status}</span>`;
  return `<span class="badge">${status}</span>`;
}

// ── Truncate long description ────────────────────────────────────
function trunc(str, n = 50) {
  if (!str) return "—";
  return str.length > n ? str.slice(0, n) + "…" : str;
}

// ── Escape HTML ──────────────────────────────────────────────────
function esc(str) {
  if (!str) return "";
  return str.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;");
}

// ═══════════════════════════════════════════════════════════════
//  1. ADD CRIME
// ═══════════════════════════════════════════════════════════════
async function addCrime() {
  const type        = document.getElementById("add-type").value.trim();
  const location    = document.getElementById("add-location").value.trim();
  const description = document.getElementById("add-description").value.trim();
  const status      = document.getElementById("add-status").value;

  if (!type || !location) {
    showToast("Crime Type and Location are required.", "error"); return;
  }

  try {
    const res  = await fetch(API, {
      method:  "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body:    `type=${enc(type)}&location=${enc(location)}&description=${enc(description)}&status=${enc(status)}`
    });
    const data = await res.json();
    if (data.success) {
      showToast("✔ Case added to database.", "success");
      clearAddForm();
    } else {
      showToast("Error: " + data.message, "error");
    }
  } catch (e) {
    showToast("Connection error. Is the server running?", "error");
  }
}

function clearAddForm() {
  ["add-type","add-location","add-description"].forEach(id => document.getElementById(id).value = "");
  document.getElementById("add-status").value = "Open";
}

// ═══════════════════════════════════════════════════════════════
//  2. VIEW ALL CRIMES
// ═══════════════════════════════════════════════════════════════
async function loadCrimes() {
  const tbody = document.getElementById("crimes-table-body");
  tbody.innerHTML = `<tr><td colspan="6" class="empty-state"><span class="empty-icon">⬡</span>Loading...</td></tr>`;

  try {
    const res   = await fetch(API);
    const cases = await res.json();

    if (!cases || cases.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" class="empty-state"><span class="empty-icon">⬡</span>No records found.</td></tr>`;
      return;
    }

    tbody.innerHTML = cases.map(c => `
      <tr>
        <td class="td-id">#${c.id}</td>
        <td>${esc(c.type)}</td>
        <td>${esc(c.location)}</td>
        <td style="max-width:220px;color:var(--text-dim);font-size:.85rem">${trunc(c.description)}</td>
        <td>${badgeFor(c.status)}</td>
        <td class="td-actions">
          <button class="btn btn-edit"   onclick="openEditFromTable(${c.id})">Edit</button>
          <button class="btn btn-danger" onclick="quickDelete(${c.id})">Del</button>
        </td>
      </tr>
    `).join("");

  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state" style="color:var(--red)">Connection error. Is the server running?</td></tr>`;
  }
}

// ═══════════════════════════════════════════════════════════════
//  3. UPDATE CRIME
// ═══════════════════════════════════════════════════════════════
async function loadCaseForEdit() {
  const id = document.getElementById("update-search-id").value.trim();
  if (!id) { showToast("Enter a Case ID.", "error"); return; }

  try {
    const res  = await fetch(`${API}?id=${enc(id)}`);
    const data = await res.json();

    if (!data.success) {
      showToast("Case not found: " + data.message, "error");
      document.getElementById("update-form-section").style.display = "none";
      return;
    }

    const c = data.data;
    document.getElementById("update-id").value          = c.id;
    document.getElementById("update-type").value        = c.type;
    document.getElementById("update-location").value    = c.location;
    document.getElementById("update-description").value = c.description;
    document.getElementById("update-status").value      = c.status;
    document.getElementById("update-form-section").style.display = "block";
    showToast(`Case #${c.id} loaded.`, "success");

  } catch (e) {
    showToast("Connection error.", "error");
  }
}

// Called from View tab Edit button
function openEditFromTable(id) {
  switchTab("update");
  document.getElementById("update-search-id").value = id;
  loadCaseForEdit();
}

async function updateCrime() {
  const id          = document.getElementById("update-id").value;
  const type        = document.getElementById("update-type").value.trim();
  const location    = document.getElementById("update-location").value.trim();
  const description = document.getElementById("update-description").value.trim();
  const status      = document.getElementById("update-status").value;

  if (!type || !location) {
    showToast("Type and Location cannot be empty.", "error"); return;
  }

  try {
    const res  = await fetch(API, {
      method:  "PUT",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body:    `id=${enc(id)}&type=${enc(type)}&location=${enc(location)}&description=${enc(description)}&status=${enc(status)}`
    });
    const data = await res.json();
    if (data.success) {
      showToast("✔ " + data.message, "success");
      document.getElementById("update-form-section").style.display = "none";
      document.getElementById("update-search-id").value = "";
    } else {
      showToast("Error: " + data.message, "error");
    }
  } catch (e) {
    showToast("Connection error.", "error");
  }
}

// ═══════════════════════════════════════════════════════════════
//  4. DELETE CRIME
// ═══════════════════════════════════════════════════════════════
let _deleteTargetId = null;

function confirmDelete() {
  const id = document.getElementById("delete-id").value.trim();
  if (!id) { showToast("Enter a Case ID to delete.", "error"); return; }

  _deleteTargetId = id;
  document.getElementById("confirm-text").textContent =
    `⚠ Are you sure you want to permanently delete Case #${id}? This cannot be undone.`;
  document.getElementById("confirm-box").style.display = "block";
}

function cancelDelete() {
  _deleteTargetId = null;
  document.getElementById("confirm-box").style.display = "none";
}

async function deleteCrime() {
  if (!_deleteTargetId) return;
  const id = _deleteTargetId;
  cancelDelete();

  try {
    const res  = await fetch(`${API}?id=${enc(id)}`, { method: "DELETE" });
    const data = await res.json();
    if (data.success) {
      showToast("✔ " + data.message, "success");
      document.getElementById("delete-id").value = "";
    } else {
      showToast("Error: " + data.message, "error");
    }
  } catch (e) {
    showToast("Connection error.", "error");
  }
}

// Quick delete from View tab table
function quickDelete(id) {
  switchTab("delete");
  document.getElementById("delete-id").value = id;
  confirmDelete();
}

// ── URL encode helper ────────────────────────────────────────────
function enc(s) { return encodeURIComponent(s); }

// ── Load on View tab by default ──────────────────────────────────
window.onload = () => {
  // nothing auto-loaded; user clicks tabs
};
