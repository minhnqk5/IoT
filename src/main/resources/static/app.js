let me = JSON.parse(localStorage.getItem("iotUser") || "null");

let sortS = "desc";
let sortH = "desc";

let chart = null;
let controlLoading = false;

// ==============================
// SENSOR PAGINATION / FILTER
// ==============================

let sensorPage = 0;
let sensorPageSize = 10;

// ==============================
// HISTORY PAGINATIaON / FILTER
// ==============================

let historyPage = 0;
let historyPageSize = 10;

const $ = (id) => document.getElementById(id);

// Tao cac hang trong bang tu danh sach du lieu va cau hinh cot.
function renderTable(bodyId, rows, columns) {
  const body = $(bodyId);

  if (!rows.length) {
    body.innerHTML = `<tr><td colspan="${columns.length}" style="text-align:center">Không có dữ liệu</td></tr>`;
    return;
  }

  body.innerHTML = rows
    .map(
      (row) => `
    <tr>${columns
      .map((column) => {
        const value = row[column.key];
        const content = column.format ? column.format(value, row) : value;
        return `<td>${content}</td>`;
      })
      .join("")}</tr>
  `,
    )
    .join("");
}

// Cap nhat thong tin trang va trang thai nut truoc/sau.
function renderPagination(prefix, result) {
  const currentPage = result.page ?? 0;
  const totalPages = Math.max(1, result.totalPages || 1);

  $(`${prefix}pageinfo`).textContent =
    `Trang ${currentPage + 1} / ${totalPages}`;
  $(`${prefix}prev`).disabled = currentPage <= 0;
  $(`${prefix}next`).disabled = currentPage >= totalPages - 1;
}

// ======================================================
// LOGIN STATE
// ======================================================

if (!me) {
  $("login").classList.remove("hidden");
  $("app").classList.add("hidden");
} else {
  start();
}

// ======================================================
// LOGIN
// ======================================================

$("loginForm").onsubmit = async (event) => {
  event.preventDefault();

  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: $("user").value,
        password: $("pass").value,
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "Đăng nhập thất bại");
    }

    localStorage.setItem("iotUser", JSON.stringify(data));

    location.reload();
  } catch (error) {
    $("err").textContent = error.message;
  }
};

// ======================================================
// LOGOUT
// ======================================================

$("logout").onclick = () => {
  localStorage.removeItem("iotUser");
  location.reload();
};

// ======================================================
// START
// ======================================================

function start() {
  $("login").classList.add("hidden");
  $("app").classList.remove("hidden");

  $("who").textContent = me.username + " · " + me.role;

  document.querySelectorAll("nav a").forEach((link) => {
    link.onclick = () => {
      show(link.dataset.page);
    };
  });

  //location.hash đọc phần hash trong URL, sau #
  show(location.hash.slice(1) || "dashboard");

  // Dashboard refresh
  setInterval(() => {
    if (location.hash.slice(1) === "dashboard") {
      dashboard();
    }
  }, 2000);
}

// ======================================================
// CHUYỂN TRANG
// ======================================================

function show(page) {
  const pageElement = $(page);

  if (!pageElement) {
    page = "dashboard";
  }

  document.querySelectorAll(".page").forEach((element) => {
    element.classList.add("hidden");
  });

  $(page).classList.remove("hidden");

  $("title").textContent = {
    dashboard: "Dashboard",
    datasensor: "Data Sensor",
    history: "History",
    profile: "Profile",
  }[page];

  location.hash = page;

  if (page === "dashboard") {
    dashboard();
  }

  if (page === "datasensor") {
    sensorPage = 0;
    sensors();
  }

  if (page === "history") {
    historyPage = 0;
    loadHistory();
  }
}

// ======================================================
// API
// ======================================================

async function api(url, options = {}) {
  const response = await fetch(url, options);

  const text = await response.text();

  let data;

  try {
    data = text ? JSON.parse(text) : {};
  } catch (error) {
    console.error("API trả về dữ liệu không phải JSON:", text);

    throw new Error("Server trả về dữ liệu không hợp lệ");
  }

  if (!response.ok) {
    throw new Error(data.message || data.error || `HTTP ${response.status}`);
  }

  return data;
}

// ======================================================
// FORMAT TIME
// ======================================================

function time(value) {
  if (!value) {
    return "";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const dd = String(date.getDate()).padStart(2, "0");

  const mm = String(date.getMonth() + 1).padStart(2, "0");

  const yyyy = date.getFullYear();

  const hh = String(date.getHours()).padStart(2, "0");

  const mi = String(date.getMinutes()).padStart(2, "0");

  const ss = String(date.getSeconds()).padStart(2, "0");

  return `${hh}:${mi}:${ss} ${dd}/${mm}/${yyyy}`;
}

// ======================================================
// VALIDATE DATE
// ======================================================

function validDate(value) {
  if (!value) {
    return true;
  }

  if (!/^\d{2}\/\d{2}\/\d{4}$/.test(value)) {
    return false;
  }

  const [dd, mm, yyyy] = value.split("/").map(Number);

  const date = new Date(yyyy, mm - 1, dd);

  return (
    date.getFullYear() === yyyy &&
    date.getMonth() === mm - 1 &&
    date.getDate() === dd
  );
}

// ======================================================
// VALIDATE TIME FILTER
// Chấp nhận: dd/MM/yyyy | HH:mm dd/MM/yyyy | HH:mm:ss dd/MM/yyyy
// ======================================================

function validTimeFilter(value) {
  if (!value) {
    return true;
  }

  const match =
    /^(?:(\d{2}):(\d{2})(?::(\d{2}))? )?(\d{2}\/\d{2}\/\d{4})$/.exec(value);

  if (!match) {
    return false;
  }

  const hh = match[1] === undefined ? 0 : Number(match[1]);
  const mi = match[2] === undefined ? 0 : Number(match[2]);
  const ss = match[3] === undefined ? 0 : Number(match[3]);

  return hh <= 23 && mi <= 59 && ss <= 59 && validDate(match[4]);
}

// ======================================================
// THANH TÌM KIẾM (dùng chung cho Data Sensor và History)
//   prefix "s" = Data Sensor, "h" = History
//   <select>: Tất cả / từng loại (hoặc thiết bị) / Thời gian
//   <input> : giá trị cần tìm, hoặc thời gian nếu chọn "Thời gian"
// ======================================================

const TIME_HINT = "Thời gian: dd/MM/yyyy hoặc HH:mm[:ss] dd/MM/yyyy";

// Trả về { field, text, time } hoặc null nếu thời gian nhập sai
//   field: loại cảm biến / thiết bị đang chọn ("" = Tất cả)
//   text : giá trị cần tìm (khi không chọn "Thời gian")
//   time : thời gian cần tìm (khi chọn "Thời gian")
//   timeOnly: ô nhập luôn là thời gian (trang History)
function readSearch(prefix, timeOnly = false) {
  const field = $(prefix + "field").value;
  const text = $(prefix + "text")
    .value.trim()
    .replace(/\s+/g, " ");

  // Nếu search giá trị ở Datasensor 
  if (!timeOnly && field !== "time") {
    return { field, text, time: "" };
  }

  if (!validTimeFilter(text)) {
    alert(
      "Thời gian phải có dạng dd/MM/yyyy, HH:mm dd/MM/yyyy hoặc HH:mm:ss dd/MM/yyyy",
    );
    return null;
  }

  //Nếu search thời gian
  return { field: timeOnly ? field : "", text: "", time: text };
}

// Đổi lựa chọn -> đổi gợi ý trong ô nhập rồi tìm lại
function setupSearchField(prefix, valueHint, reload) {
  const select = $(prefix + "field");
  const input = $(prefix + "text");
  let wasTime = false;

  select.onchange = () => {
    const isTime = select.value === "time";

    // Đổi giữa "tìm theo thời gian" và "tìm theo giá trị" thì xóa ô nhập
    if (isTime !== wasTime) {
      input.value = "";
    }

    wasTime = isTime;
    input.placeholder = isTime ? TIME_HINT : valueHint;

    reload();
  };
}

// ======================================================
// DASHBOARD
// ======================================================

async function dashboard() {
  try {
    const data = await api("/api/sensors/dashboard");

    const latest = {};

    (data.latest || []).forEach((sensor) => {
      latest[sensor.sensorType] = sensor;
    });

    $("temp").textContent = latest.temperature?.value ?? "--";

    $("hum").textContent = latest.humidity?.value ?? "--";

    $("light").textContent = latest.light?.value ?? "--";

    renderChart(data.chart || []);
  } catch (error) {
    console.error("Dashboard sensor error:", error);
  }

  // LED tách riêng.
  // Nếu LED lỗi thì sensor vẫn hiển thị.
  if (!controlLoading) {
    try {
      await renderLeds();
    } catch (error) {
      console.error("Dashboard LED error:", error);
    }
  }
}

// ======================================================
// CHART
// ======================================================


function renderChart(rows) {
  const sensorTypes = ["temperature", "humidity", "light"];

  //15 mốc thời gian
  const labels = rows
    .filter((row) => row.sensorType === "temperature")
    .slice()
    .reverse()
    .map((row) => time(row.createdAt));
  
  //15 mốc dữ liệu của từng loại cảm biến
  const sets = sensorTypes.map((type) => ({
    label: type,
    data: rows
      .filter((row) => row.sensorType === type)
      .slice()
      .reverse()
      .map((row) => row.value),
    tension: 0,
  }));

  // Lần đầu: tạo biểu đồ
  if (!chart) {
    chart = new Chart($("chart"), {
      type: "line",
      data: {
        labels: labels,
        datasets: sets,
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
      },
    });

    return;
  }

  // Những lần sau: chỉ cập nhật dữ liệu biểu đồ cũ
  chart.data.labels = labels;

  chart.data.datasets.forEach((dataset, index) => {
    dataset.data = sets[index].data;
  });

  chart.update("none");
}
;

// ======================================================
// HIỂN THỊ LED
// ======================================================

async function renderLeds() {
  const result = await api("/api/devices/history?page=0&size=100&sort=desc");

  const rows = result.content;

  const states = {
    LED01: "OFF",
    LED02: "OFF",
  };

  //Tìm thấy bản ghi SUCCESS mới nhất của 2 đèn thì thôi, không cần tìm tiếp.
  const seen = {
    LED01: false,
    LED02: false,
  };

  //Tìm bản ghi SUCCESS mới nhất của 2 đèn
  for (const row of rows) {
    if (
      row.status === "SUCCESS" &&
      states[row.deviceId] !== undefined &&
      !seen[row.deviceId]
    ) {
      states[row.deviceId] = row.action;

      seen[row.deviceId] = true;
    }
  }

  $("leds").innerHTML = ["LED01", "LED02"]
    .map((id) => {
      const isOn = states[id] === "ON";

      return `
                    <div class="device">

                        <b>
                            ${id === "LED01" ? "LED 1" : "LED 2"}

                            <small>
                                ${states[id]}
                            </small>
                        </b>

                        <button
                            class="${isOn ? "on" : ""}"
                            ${controlLoading ? "disabled" : ""}
                            onclick="
                                control(
                                    '${id}',
                                    '${isOn ? "OFF" : "ON"}'
                                )
                            "
                        >
                            ${isOn ? "OFF" : "ON"}
                        </button>

                    </div>
                `;
    })
    .join("");

  if (controlLoading) {
    showLedLoading();
  }
}

// ======================================================
// LED LOADING
// ======================================================

function showLedLoading() {
  document.querySelectorAll("#leds .device").forEach((device) => {
    device.classList.add("loading");

    const button = device.querySelector("button");

    if (button) {
      button.disabled = true;
      button.textContent = "...";
    }
  });
}

// ======================================================
// ĐIỀU KHIỂN LED
// ======================================================

async function control(id, action) {
  if (controlLoading) {
    return;
  }

  controlLoading = true;

  showLedLoading();

  try {
    const result = await api("/api/devices/" + id + "/control", {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        action,
      }),
    });

    if (result.status !== "PENDING") {
      throw new Error("Không tạo được lệnh điều khiển");
    }

    /*
     * result.historyId (ControlResponse) là ID History
     * của command vừa tạo.
     */
    const success = await waitForResult(id, result.historyId ?? result.id);

    if (!success) {
      alert("Không nhận được phản hồi từ ESP8266");
    }

    await renderLeds();

    if (location.hash.slice(1) === "history") {
      await loadHistory();
    }
  } catch (error) {
    console.error(error);

    alert(error.message);
  } finally {
    controlLoading = false;

    try {
      await renderLeds();
    } catch (error) {
      console.error(error);
    }
  }
}

// ======================================================
// CHỜ ESP PHẢN HỒI
// ======================================================

async function waitForResult(deviceId, historyId) {
  const start = Date.now();

  while (Date.now() - start < 5000) {
    await new Promise((resolve) => setTimeout(resolve, 500));

    try {
      const result = await api(
        "/api/devices/history" +
          "?device=" +
          deviceId +
          "&page=0" +
          "&size=50" +
          "&sort=desc",
      );

      const rows = result.content || [];

      const current = rows.find((row) => Number(row.id) === Number(historyId));

      if (!current) {
        continue;
      }

      if (current.status === "SUCCESS") {
        return true;
      }

      if (current.status === "FAIL") {
        return false;
      }
    } catch (error) {
      console.error("Polling history error:", error);
    }
  }

  return false;
}

// ======================================================
// SENSOR DATA
// ======================================================

async function sensors() {
  const search = readSearch("s");

  if (search === null) {
    return;
  }

  sensorPageSize = Math.max(1, Number($("spagesize").value) || 10);

  const query = new URLSearchParams({
    page: sensorPage,

    size: sensorPageSize,

    sort: sortS,
  });

  // Loại cảm biến đang chọn (bỏ trống = tất cả loại)
  if (search.field) {
    query.set("type", search.field);
  }

  // Tìm theo giá trị
  if (search.text) {
    query.set("value", search.text);
  }

  // Tìm theo thời gian
  if (search.time) {
    query.set("time", search.time);
  }

  try {
    const result = await api("/api/sensors/data?" + query.toString());

    const rows = result.content || [];

    renderTable("sbody", rows, [
      { key: "id" },
      { key: "sensorType" },
      { key: "value" },
      { key: "unit" },
      { key: "createdAt", format: (value) => time(value) },
    ]);

    renderPagination("s", result);
  } catch (error) {
    console.error(error);

    alert(error.message);
  }
}

// ======================================================
// SENSOR SEARCH
// ======================================================

$("ssearch").onclick = () => {
  sensorPage = 0;

  sensors();
};

// ======================================================
// SENSOR SORT
// ======================================================

$("ssort").onclick = () => {
  sortS = sortS === "desc" ? "asc" : "desc";

  $("ssort").textContent = sortS === "desc" ? "Mới nhất" : "Cũ nhất";

  sensorPage = 0;

  sensors();
};

// ======================================================
// SENSOR SEARCH FIELD
// ======================================================

setupSearchField("s", "Nhập giá trị cần tìm, hoặc chọn Thời gian", () => {
  sensorPage = 0;

  sensors();
});

// ======================================================
// SENSOR PAGINATION
// ======================================================

$("sprev").onclick = () => {
  if (sensorPage > 0) {
    sensorPage--;

    sensors();
  }
};

$("snext").onclick = () => {
  if (!$("snext").disabled) {
    sensorPage++;

    sensors();
  }
};

$("spagesize").onchange = () => {
  sensorPage = 0;

  sensors();
};

// ======================================================
// HISTORY
// ======================================================

async function loadHistory() {
  const search = readSearch("h", true);

  if (search === null) {
    return;
  }

  historyPageSize = Math.max(1, Number($("hpagesize").value) || 10);

  const query = new URLSearchParams({
    page: historyPage,

    size: historyPageSize,

    sort: sortH,
  });

  // Thiết bị đang chọn (bỏ trống = tất cả thiết bị)
  if (search.field) {
    query.set("device", search.field);
  }

  // Lọc theo Action (ON / OFF) và Status (PENDING / SUCCESS / FAIL)
  if ($("haction").value) {
    query.set("action", $("haction").value);
  }

  if ($("hstatus").value) {
    query.set("status", $("hstatus").value);
  }

  // Tìm theo thời gian
  if (search.time) {
    query.set("time", search.time);
  }

  try {
    const result = await api("/api/devices/history?" + query.toString());

    const rows = result.content || [];

    renderTable("hbody", rows, [
      { key: "id" },
      { key: "deviceName" },
      { key: "action" },
      { key: "status" },
      { key: "createdAt", format: (value) => time(value) },
    ]);

    renderPagination("h", result);
  } catch (error) {
    console.error(error);

    alert(error.message);
  }
}

// ======================================================
// HISTORY SEARCH
// ======================================================

$("hsearch").onclick = () => {
  historyPage = 0;

  loadHistory();
};

// ======================================================
// HISTORY SORT
// ======================================================

$("hsort").onclick = () => {
  sortH = sortH === "desc" ? "asc" : "desc";

  $("hsort").textContent = sortH === "desc" ? "Mới nhất" : "Cũ nhất";

  historyPage = 0;

  loadHistory();
};

// ======================================================
// HISTORY FILTERS (thiết bị, action, status): chọn là lọc lại ngay
// ======================================================

["hfield", "haction", "hstatus"].forEach((id) => {
  $(id).onchange = () => {
    historyPage = 0;

    loadHistory();
  };
});

// ======================================================
// HISTORY PAGINATION
// ======================================================

$("hprev").onclick = () => {
  if (historyPage > 0) {
    historyPage--;

    loadHistory();
  }
};

$("hnext").onclick = () => {
  if (!$("hnext").disabled) {
    historyPage++;

    loadHistory();
  }
};

$("hpagesize").onchange = () => {
  historyPage = 0;

  loadHistory();
};

// Nhấn Enter ở ô tìm kiếm để tìm
$("stext").addEventListener("keydown", (e) => {
  if (e.key === "Enter") $("ssearch").click();
});

$("htext").addEventListener("keydown", (e) => {
  if (e.key === "Enter") $("hsearch").click();
});
