/*
  events.js

  Usage:
    <script src="/events.js"></script>

  Injects a floating, draggable, resizable events window that loads JSON from /events.
*/

(function () {
  const ROOT_ID = 'events-overlay-root';

  if (document.getElementById(ROOT_ID)) {
    // Already mounted.
    return;
  }

  function el(tag, attrs, children) {
    const node = document.createElement(tag);
    if (attrs) {
      for (const [k, v] of Object.entries(attrs)) {
        if (k === 'class') node.className = v;
        else if (k === 'style') node.setAttribute('style', v);
        else if (k.startsWith('on') && typeof v === 'function') node.addEventListener(k.slice(2), v);
        else node.setAttribute(k, v);
      }
    }
    if (children != null) {
      const arr = Array.isArray(children) ? children : [children];
      for (const c of arr) {
        if (c == null) continue;
        node.appendChild(typeof c === 'string' ? document.createTextNode(c) : c);
      }
    }
    return node;
  }

  function injectStyle() {
    const style = el('style', { id: 'events-overlay-style' });
    style.textContent = `
:root {
  --events-text: #1b1f24;
  --events-muted: rgba(27, 31, 36, 0.72);
  --events-border: rgba(27, 31, 36, 0.18);
  --events-accent: #0b66c3;
  --events-window: rgba(255, 255, 255, 0.86);
  --events-window-strong: rgba(255, 255, 255, 0.96);
  --events-shadow: 0 18px 48px rgba(0, 0, 0, 0.28);
}

#${ROOT_ID} {
  position: fixed;
  inset: 0;
  pointer-events: none; /* allow clicks through except on window */
  z-index: 2147483000;
}

#${ROOT_ID} .events-window {
  pointer-events: auto;
  position: fixed;
  left: 24px;
  top: 24px;
  width: min(520px, calc(100vw - 48px));
  height: min(620px, calc(100vh - 48px));
  min-width: 360px;
  min-height: 240px;
  background: var(--events-window);
  border: 1px solid var(--events-border);
  border-radius: 14px;
  box-shadow: var(--events-shadow);
  overflow: auto;
  resize: both;
  backdrop-filter: blur(7px);
  color: var(--events-text);
  font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial,
    "Apple Color Emoji", "Segoe UI Emoji";
}

#${ROOT_ID} .events-window-header {
  position: sticky;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 10px;
  background: linear-gradient(var(--events-window-strong), rgba(255, 246, 208, 0.9));
  border-bottom: 1px solid var(--events-border);
  cursor: grab;
  user-select: none;
  touch-action: none;
  z-index: 2;
}

#${ROOT_ID} .events-window-header:active { cursor: grabbing; }

#${ROOT_ID} .events-header-left {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

#${ROOT_ID} .events-window-title {
  font-size: 14px;
  font-weight: 700;
}

#${ROOT_ID} .events-status {
  color: var(--events-muted);
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 40vw;
}

#${ROOT_ID} .events-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

#${ROOT_ID} button {
  background: rgba(255, 255, 255, 0.55);
  color: var(--events-text);
  border: 1px solid var(--events-border);
  padding: 6px 8px;
  border-radius: 10px;
  cursor: pointer;
}

#${ROOT_ID} button:hover { border-color: rgba(11, 102, 195, 0.55); }

#${ROOT_ID} .events-window-body {
  padding: 10px;
}

#${ROOT_ID} .events-error {
  background: rgba(216, 44, 67, 0.10);
  border: 1px solid rgba(216, 44, 67, 0.35);
  padding: 8px 10px;
  border-radius: 10px;
  margin: 0 0 10px 0;
  white-space: pre-wrap;
}

#${ROOT_ID} table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  border: 1px solid var(--events-border);
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  overflow: hidden;
}

#${ROOT_ID} th, #${ROOT_ID} td {
  text-align: left;
  padding: 6px 8px;
  border-bottom: 1px solid var(--events-border);
  font-size: 13px;
  line-height: 1.2;
}

#${ROOT_ID} th {
  color: var(--events-muted);
  font-weight: 700;
  font-size: 11px;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  background: rgba(245, 211, 94, 0.35);
}

#${ROOT_ID} tr:hover td { background: rgba(245, 211, 94, 0.22); }

#${ROOT_ID} a { color: var(--events-accent); text-decoration: none; }
#${ROOT_ID} a:hover { text-decoration: underline; }
#${ROOT_ID} .events-muted { color: var(--events-muted); }

#${ROOT_ID} dialog {
  border: 1px solid var(--events-border);
  border-radius: 12px;
  background: var(--events-window-strong);
  box-shadow: 0 22px 70px rgba(0, 0, 0, 0.35);
  padding: 12px 12px;
  max-width: min(520px, 90vw);
  color: var(--events-text);
}

#${ROOT_ID} dialog::backdrop {
  background: rgba(0, 0, 0, 0.35);
}
    `.trim();
    document.head.appendChild(style);
  }

  function pick(obj, keys) {
    for (const k of keys) {
      if (obj && Object.prototype.hasOwnProperty.call(obj, k) && obj[k] != null) return obj[k];
    }
    return undefined;
  }

  function toDisplayDate(v) {
    if (v == null) return '';

    function isoDateOnly(d) {
      return d.toISOString().slice(0, 10);
    }

    if (typeof v === 'string') {
      if (v.length >= 10 && /^\d{4}-\d{2}-\d{2}/.test(v)) return v.slice(0, 10);
      const d = new Date(v);
      if (!Number.isNaN(d.getTime())) return isoDateOnly(d);
      return v;
    }

    if (typeof v === 'number' && Number.isFinite(v)) {
      const ms = v < 1e12 ? v * 1000 : v;
      const d = new Date(ms);
      if (!Number.isNaN(d.getTime())) return isoDateOnly(d);
      return String(v);
    }

    if (typeof v === 'object') {
      if (typeof v.epochMillis === 'number') return toDisplayDate(v.epochMillis);
      if (typeof v.epochSecond === 'number') return toDisplayDate(v.epochSecond);
      if (typeof v.time === 'number') return toDisplayDate(v.time);
      if (typeof v.instant === 'string') return toDisplayDate(v.instant);
    }

    try {
      return JSON.stringify(v);
    } catch (_) {
      return String(v);
    }
  }

  function toUnixSeconds(v) {
    if (v == null) return null;

    if (typeof v === 'number' && Number.isFinite(v)) {
      const sec = v >= 1e12 ? Math.floor(v / 1000) : Math.floor(v);
      return Number.isFinite(sec) ? sec : null;
    }

    if (typeof v === 'string') {
      if (/^\d{10,13}$/.test(v)) return toUnixSeconds(Number(v));
      const d = new Date(v);
      if (!Number.isNaN(d.getTime())) return Math.floor(d.getTime() / 1000);
      return null;
    }

    if (typeof v === 'object') {
      if (typeof v.epochSecond === 'number') return Math.floor(v.epochSecond);
      if (typeof v.epochMillis === 'number') return Math.floor(v.epochMillis / 1000);
      if (typeof v.time === 'number') return toUnixSeconds(v.time);
      if (typeof v.instant === 'string') return toUnixSeconds(v.instant);
    }

    return null;
  }

  function chartCellValue(ev) {
    const v = pick(ev, ['chart', 'chartId', 'chart-id', 'chart_id', 'chartid', 'id']);
    if (v == null) return { text: '', href: null };

    if (typeof v === 'object') {
      const url = pick(v, ['url', 'href']);
      const id = pick(v, ['id', 'chartId', 'chart-id']);
      if (url) return { text: String(id ?? url), href: String(url) };
      return { text: JSON.stringify(v), href: null };
    }

    const s = String(v);
    if (s.startsWith('http://') || s.startsWith('https://') || s.startsWith('/')) {
      return { text: s, href: s };
    }

    return { text: s, href: null };
  }

  function makeWindowDraggable(win, handle) {
    if (!win || !handle) return;

    let dragging = false;
    let startX = 0;
    let startY = 0;
    let startLeft = 0;
    let startTop = 0;

    handle.addEventListener('pointerdown', (e) => {
      if (e.button != null && e.button !== 0) return;
      if (e.target && e.target.closest && e.target.closest('button,a,input,select,textarea,label')) return;

      dragging = true;
      const rect = win.getBoundingClientRect();
      startLeft = rect.left;
      startTop = rect.top;
      startX = e.clientX;
      startY = e.clientY;

      win.style.left = `${startLeft}px`;
      win.style.top = `${startTop}px`;
      win.style.right = 'auto';
      win.style.bottom = 'auto';

      handle.setPointerCapture?.(e.pointerId);
      e.preventDefault();
    });

    handle.addEventListener('pointermove', (e) => {
      if (!dragging) return;
      const dx = e.clientX - startX;
      const dy = e.clientY - startY;
      win.style.left = `${startLeft + dx}px`;
      win.style.top = `${startTop + dy}px`;
    });

    function stopDrag() {
      dragging = false;
    }

    handle.addEventListener('pointerup', stopDrag);
    handle.addEventListener('pointercancel', stopDrag);
    handle.addEventListener('lostpointercapture', stopDrag);
  }

  function mount() {
    injectStyle();

    const root = el('div', { id: ROOT_ID });

    const statusEl = el('div', { class: 'events-status' }, 'Loading…');
    const reloadBtn = el('button', { type: 'button' }, 'Reload');

    const header = el(
      'div',
      {
        class: 'events-window-header',
        title: 'Drag to move. Resize from the bottom-right corner.'
      },
      [
        el('div', { class: 'events-header-left' }, [
          el('div', { class: 'events-window-title' }, 'Events'),
          statusEl
        ]),
        el('div', { class: 'events-controls' }, [reloadBtn])
      ]
    );

    const errorEl = el('div', { class: 'events-error', style: 'display:none' });

    const bodyEl = el('tbody', { id: 'events-overlay-body' }, [
      el('tr', null, el('td', { colspan: '4', class: 'events-muted' }, 'Loading…'))
    ]);

    const table = el('table', { 'aria-label': 'events' }, [
      el('thead', null,
        el('tr', null, [
          el('th', null, 'Asset'),
          el('th', null, 'Unix'),
          el('th', null, 'Date'),
          el('th', null, 'Chart')
        ])
      ),
      bodyEl
    ]);

    const dialogContent = el('div', {
      id: 'events-overlay-dialog-content',
      style:
        "font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace; font-size: 12px; white-space: pre-wrap;"
    });

    const dialog = el('dialog', { id: 'events-overlay-dialog', 'aria-label': 'event details' }, [
      el('form', { method: 'dialog', style: 'margin:0' }, [
        el('div', { style: 'display:flex; justify-content:space-between; align-items:center; gap:12px; margin-bottom:10px;' }, [
          el('div', { style: 'font-weight:700;' }, 'Event'),
          el('button', { type: 'submit', value: 'close' }, 'Close')
        ]),
        dialogContent
      ])
    ]);

    const winBody = el('div', { class: 'events-window-body' }, [errorEl, table, dialog]);

    const win = el('div', { class: 'events-window', role: 'dialog', 'aria-label': 'events window' }, [
      header,
      winBody
    ]);

    root.appendChild(win);
    document.body.appendChild(root);

    makeWindowDraggable(win, header);

    function setStatus(text) {
      statusEl.textContent = text;
    }

    function showError(err) {
      errorEl.style.display = 'block';
      errorEl.textContent = String(err && err.stack ? err.stack : err);
    }

    function hideError() {
      errorEl.style.display = 'none';
      errorEl.textContent = '';
    }

    function showRowDialog(evObj) {
      const asset = pick(evObj, ['asset', 'symbol', 'ticker', 'instrument']) ?? '';
      const dateRaw = pick(evObj, ['date', 'date-instant', 'dateInstant', 'datetime', 'time', 'ts', 'timestamp']);
      const unixSeconds = toUnixSeconds(dateRaw);

      const lines = [
        `asset: ${String(asset)}`,
        `unix:  ${unixSeconds == null ? 'n/a' : unixSeconds}`
      ];

      if (dialog && typeof dialog.showModal === 'function') {
        dialogContent.textContent = lines.join('\n');
        dialog.showModal();
      } else {
        alert(lines.join('\n'));
      }
    }

    function getTvWidget() {
      // tvtrading.html stores it as window.tvWidget
      return (typeof window !== 'undefined' && window.tvWidget) ? window.tvWidget : null;
    }

    function waitForTvWidgetReady(widget) {
      // Charting Library exposes widget.onChartReady(cb)
      return new Promise((resolve) => {
        if (!widget) return resolve();
        if (typeof widget.onChartReady === 'function') {
          widget.onChartReady(() => resolve());
        } else {
          resolve();
        }
      });
    }

    function parseJsonIfLooksLikeJson(v) {
      if (typeof v !== 'string') return v;
      const s = v.trim();
      if (!s) return v;
      if (!(s.startsWith('{') || s.startsWith('['))) return v;
      try {
        return JSON.parse(s);
      } catch (_) {
        return v;
      }
    }

    function getChartRefFromEvent(evObj) {
      return pick(evObj, ['chart', 'chartId', 'chart-id', 'chart_id', 'chartid']);
    }

    async function loadTradingViewLayoutFromEvent(evObj) {
      const widget = getTvWidget();
      if (!widget) return false;

      await waitForTvWidgetReady(widget);

      const chartRef = getChartRefFromEvent(evObj);
      if (chartRef == null) return false;

      // If the event already contains a full chart "state/layout" object, use widget.load(state).
      // (The Charting Library widget API supports widget.load(state)).
      if (typeof chartRef === 'object') {
        const stateMaybe = pick(chartRef, ['state', 'layout', 'content', 'data']);
        const parsed = parseJsonIfLooksLikeJson(stateMaybe ?? chartRef);
        if (parsed && typeof parsed === 'object' && typeof widget.load === 'function') {
          setStatus('Loading chart…');
          await widget.load(parsed);
          setStatus('Chart loaded.');
          return true;
        }
      }

      // If chartRef is JSON string representing a saved state, load it.
      if (typeof chartRef === 'string') {
        const parsed = parseJsonIfLooksLikeJson(chartRef);
        if (parsed && typeof parsed === 'object' && typeof widget.load === 'function') {
          setStatus('Loading chart…');
          await widget.load(parsed);
          setStatus('Chart loaded.');
          return true;
        }
      }

      // Otherwise assume chartRef is a saved-chart id/name and load from charts_storage.
      // The Charting Library widget API supports widget.getSavedCharts(cb) and widget.loadChartFromServer(record).
      if (typeof widget.getSavedCharts === 'function' && typeof widget.loadChartFromServer === 'function') {
        const chartId = String(chartRef);
        setStatus(`Looking up chart ${chartId}…`);

        const records = await new Promise((resolve) => {
          try {
            widget.getSavedCharts((r) => resolve(r || []));
          } catch (_) {
            resolve([]);
          }
        });

        const rec = (records || []).find((x) => String(x && x.id) === chartId || String(x && x.name) === chartId);
        if (!rec) {
          setStatus(`Chart not found: ${chartId}`);
          return false;
        }

        setStatus(`Loading chart ${chartId}…`);
        await widget.loadChartFromServer(rec);
        setStatus('Chart loaded.');
        return true;
      }

      return false;
    }

    async function focusTradingViewOnEventDate(evObj) {
      const widget = getTvWidget();
      if (!widget) return false;

      await waitForTvWidgetReady(widget);

      // Prefer an explicit unix timestamp field if your /events payload provides one.
      // Otherwise fall back to parsing the date/time fields.
      const unixRaw = pick(evObj, ['unix', 'unixSeconds', 'unix_seconds', 'epochSecond', 'epoch_second', 'time_t']);
      const dateRaw = pick(evObj, ['date', 'date-instant', 'dateInstant', 'datetime', 'time', 'ts', 'timestamp']);

      const unixSeconds = toUnixSeconds(unixRaw != null ? unixRaw : dateRaw);
      if (unixSeconds == null) return false;

      const chartApi = (typeof widget.activeChart === 'function') ? widget.activeChart() : null;
      if (!chartApi || typeof chartApi.setVisibleRange !== 'function') return false;

      let range = 86400 * 60; // fallback: ~60 days (in seconds)

      try {
        if (typeof chartApi.getVisibleRange === 'function') {
          const current = chartApi.getVisibleRange();
          if (current && Number.isFinite(current.from) && Number.isFinite(current.to)) {
            const len = Math.max(0, current.to - current.from);
            if (len > 0) range = len;
          }
        }
      } catch (_) {
        // ignore
      }

      range = Math.max(60, Math.floor(range));

      const to = unixSeconds;
      const from = unixSeconds - range;

      console.log(`setting range to: ${unixSeconds} range: ${range} from: ${from}`);

      setStatus(`Focusing ${toDisplayDate(dateRaw ?? unixSeconds)}…`);

      // After loading a chart layout, the chart can report ready while the internals are still initializing.
      // In that case, setVisibleRange can throw (e.g. "Value is null"). We retry briefly.
      const maxAttempts = 12;
      for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
          await chartApi.setVisibleRange({ from, to });
          break;
        } catch (e) {
          console.warn(`Failed to set visible range (attempt ${attempt}/${maxAttempts}):`, e);
          if (attempt === maxAttempts) return false;
          await new Promise((r) => setTimeout(r, 200));
        }
      }

      setStatus('Ready.');
      return true;
    }

    function renderEvents(events) {
      bodyEl.innerHTML = '';

      if (!Array.isArray(events) || events.length === 0) {
        bodyEl.appendChild(el('tr', null, el('td', { colspan: '4', class: 'events-muted' }, 'No events.')));
        return;
      }

      for (const evObj of events) {
        const asset = pick(evObj, ['asset', 'symbol', 'ticker', 'instrument']) ?? '';
        const date = pick(evObj, ['date', 'date-instant', 'dateInstant', 'datetime', 'time', 'ts', 'timestamp']);
        const chart = chartCellValue(evObj);

        const tr = el('tr');
        tr.style.cursor = 'pointer';

        tr.addEventListener('click', async (e) => {
          if (e.target && e.target.closest && e.target.closest('a')) return;

          console.log(`loading chart ${String(asset)} date: ${toDisplayDate(date)}`);

          // If this page has a TradingView Charting Library widget (window.tvWidget),
          // load the chart/layout referenced by this row, then focus time on the event date.
          try {
            const loaded = await loadTradingViewLayoutFromEvent(evObj);
            if (loaded) {
              await focusTradingViewOnEventDate(evObj);
            }
          } catch (err) {
            showError(err);
          }
        });

        const unix = toUnixSeconds(date);

        const tdAsset = el('td', null, String(asset));
        const tdUnix = el('td', null, unix == null ? '' : String(unix));
        const tdDate = el('td', null, toDisplayDate(date));

        const tdChart = el('td');
        if (chart.href) {
          const a = el('a', { href: chart.href }, chart.text);
          tdChart.appendChild(a);
        } else {
          tdChart.textContent = chart.text;
        }

        tr.appendChild(tdAsset);
        tr.appendChild(tdUnix);
        tr.appendChild(tdDate);
        tr.appendChild(tdChart);
        bodyEl.appendChild(tr);
      }
    }

    async function loadEvents() {
      hideError();
      setStatus('Loading…');

      const res = await fetch('/events', {
        headers: {
          Accept: 'application/json'
        }
      });

      if (!res.ok) {
        const body = await res.text().catch(() => '');
        throw new Error(
          `GET /events failed: ${res.status} ${res.statusText}${body ? "\n\n" + body : ''}`
        );
      }

      const data = await res.json();

      let events = data;
      if (data && !Array.isArray(data) && typeof data === 'object') {
        if (Array.isArray(data.events)) events = data.events;
        else if (Array.isArray(data.data)) events = data.data;
      }

      renderEvents(events);
      setStatus(`Loaded ${Array.isArray(events) ? events.length : 0} events.`);
    }

    reloadBtn.addEventListener('click', () => {
      loadEvents().catch(showError);
    });

    // Expose for manual triggering if needed.
    window.__eventsOverlay = {
      reload: () => loadEvents().catch(showError),
      root,
      win
    };

    loadEvents().catch(showError);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', mount);
  } else {
    mount();
  }
})();
