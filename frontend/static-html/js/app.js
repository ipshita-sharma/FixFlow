/* FixFlow progressive enhancement layer. Every HTML page remains usable if this file is disabled. */
(function () {
  "use strict";

  function showToast(message) {
    var toast = document.createElement("div");
    toast.className = "toast";
    toast.setAttribute("role", "status");
    toast.textContent = message;
    document.body.appendChild(toast);
    window.setTimeout(function () { toast.remove(); }, 2800);
  }

  document.querySelectorAll("[data-menu-toggle]").forEach(function (button) {
    button.addEventListener("click", function () {
      var sidebar = document.getElementById("sidebar");
      if (sidebar) sidebar.classList.toggle("open");
    });
  });

  document.querySelectorAll("[data-toast]").forEach(function (button) {
    button.addEventListener("click", function () { showToast(button.getAttribute("data-toast")); });
  });

  document.querySelectorAll("[data-modal-open]").forEach(function (button) {
    button.addEventListener("click", function () {
      var modal = document.getElementById(button.getAttribute("data-modal-open"));
      if (modal) { modal.classList.add("is-open"); modal.setAttribute("aria-hidden", "false"); }
    });
  });
  document.querySelectorAll("[data-modal-close]").forEach(function (button) {
    button.addEventListener("click", function () {
      var modal = button.closest(".modal");
      if (modal) { modal.classList.remove("is-open"); modal.setAttribute("aria-hidden", "true"); }
    });
  });
  document.querySelectorAll(".modal").forEach(function (modal) {
    modal.addEventListener("click", function (event) { if (event.target === modal) modal.classList.remove("is-open"); });
  });

  var severity = document.querySelector("[data-severity]");
  if (severity) {
    severity.addEventListener("change", function () {
      var labels = { "1": "Low priority", "2": "Medium priority", "3": "High priority", "4": "Critical priority" };
      var widths = { "1": "28%", "2": "48%", "3": "72%", "4": "94%" };
      var preview = document.querySelector("[data-priority-preview]");
      var bar = document.querySelector("[data-priority-bar]");
      if (preview) preview.textContent = labels[severity.value] || "Waiting for severity";
      if (bar) bar.style.width = widths[severity.value] || "18%";
    });
  }

  var upload = document.querySelector("#image");
  var uploadZone = document.querySelector("[data-upload-zone]");
  if (upload && uploadZone) upload.addEventListener("change", function () {
    var fileName = upload.files && upload.files[0] ? upload.files[0].name : "";
    if (fileName) uploadZone.querySelector("strong").textContent = "Attached: " + fileName;
  });

  var search = document.querySelector("[data-table-search]");
  var table = document.querySelector("[data-filter-table]");
  var filters = document.querySelectorAll("[data-table-filter]");
  function filterRows() {
    if (!table) return;
    var query = search ? search.value.toLowerCase() : "";
    var rows = table.querySelectorAll("tbody tr");
    var shown = 0;
    rows.forEach(function (row) {
      var matchesText = row.textContent.toLowerCase().indexOf(query) !== -1;
      var matchesFilters = true;
      filters.forEach(function (filter) {
        var value = filter.value;
        if (!value) return;
        var cell = row.querySelector("[data-" + filter.getAttribute("data-table-filter") + "]");
        matchesFilters = matchesFilters && cell && cell.getAttribute("data-" + filter.getAttribute("data-table-filter")) === value;
      });
      row.hidden = !(matchesText && matchesFilters);
      if (!row.hidden) shown += 1;
    });
    var empty = table.parentElement.querySelector(".no-results");
    if (!empty) { empty = document.createElement("div"); empty.className = "no-results"; empty.textContent = "No issues match those filters."; table.parentElement.appendChild(empty); }
    empty.style.display = shown ? "none" : "block";
  }
  if (search) search.addEventListener("input", filterRows);
  filters.forEach(function (filter) { filter.addEventListener("change", filterRows); });

  document.querySelectorAll("form").forEach(function (form) {
    form.addEventListener("submit", function (event) {
      var confirmPassword = form.querySelector("#confirm-password");
      var password = form.querySelector("#register-password");
      if (confirmPassword && password && confirmPassword.value !== password.value) {
        event.preventDefault(); confirmPassword.setCustomValidity("Passwords do not match."); confirmPassword.reportValidity();
      } else if (confirmPassword) confirmPassword.setCustomValidity("");
    });
  });
}());
