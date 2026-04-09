// EFMS frontend — calls the Spring Boot REST API.
// No frameworks, just vanilla JS + fetch().

const API = '/api';
let currentBudgetId = null;

// ==================== API helper ====================
async function apiCall(path, options = {}) {
    const res = await fetch(API + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });
    const body = res.status === 204 ? null : await res.json();
    if (!res.ok) {
        throw new Error((body && body.message) || `HTTP ${res.status}`);
    }
    return body;
}

// ==================== Utility ====================
function formatMoney(n) {
    return '\u20B9 ' + Number(n).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[c]));
}

// ==================== Organizers ====================
async function loadOrganizers() {
    try {
        const organizers = await apiCall('/organizers');
        const select = document.getElementById('organizer-select');
        select.innerHTML = organizers
            .map(o => `<option value="${o.id}">${escapeHtml(o.name)} (${escapeHtml(o.email)})</option>`)
            .join('');
    } catch (err) {
        alert('Failed to load organizers: ' + err.message);
    }
}

// ==================== Create event + draft budget ====================
document.getElementById('create-event-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const payload = {
        name: fd.get('name'),
        description: fd.get('description') || null,
        startDate: fd.get('startDate'),
        endDate: fd.get('endDate'),
        organizerId: Number(fd.get('organizerId')),
        totalBudget: Number(fd.get('totalBudget'))
    };
    try {
        const budget = await apiCall('/budgets', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        currentBudgetId = budget.id;
        renderBudget(budget);
        document.getElementById('step-create').classList.add('hidden');
        document.getElementById('workspace').classList.remove('hidden');
        clearAlerts();
    } catch (err) {
        alert('Failed to create budget: ' + err.message);
    }
});

// ==================== Render budget state ====================
function renderBudget(budget) {
    document.getElementById('workspace-title').textContent = budget.eventName;
    document.getElementById('workspace-meta').textContent = `Budget #${budget.id}`;

    document.getElementById('stat-limit').textContent = formatMoney(budget.totalLimit);
    document.getElementById('stat-allocated').textContent = formatMoney(budget.allocatedTotal);
    document.getElementById('stat-remaining').textContent = formatMoney(budget.remaining);

    const badge = document.getElementById('status-badge');
    badge.textContent = budget.status;
    badge.className = 'badge ' + budget.status;

    const tbody = document.querySelector('#categories-table tbody');
    if (!budget.categories || budget.categories.length === 0) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="2">No categories yet &mdash; add one below</td></tr>';
    } else {
        tbody.innerHTML = budget.categories
            .map(c => `<tr>
                <td>${escapeHtml(c.name)}</td>
                <td class="right">${formatMoney(c.allocatedAmount)}</td>
            </tr>`)
            .join('');
    }

    // Categories can only be added while the budget is in DRAFT.
    const isDraft = budget.status === 'DRAFT';
    const addForm = document.getElementById('add-category-form');
    addForm.querySelectorAll('input, button').forEach(el => { el.disabled = !isDraft; });

    // Validate is only meaningful in DRAFT (it's what moves DRAFT -> READY).
    document.getElementById('btn-validate').disabled = !isDraft;

    // Submit is allowed from DRAFT or READY.
    const canSubmit = isDraft || budget.status === 'READY';
    document.getElementById('btn-submit').disabled = !canSubmit;
}

// ==================== Add category ====================
document.getElementById('add-category-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const payload = {
        name: fd.get('name'),
        allocatedAmount: Number(fd.get('allocatedAmount'))
    };
    try {
        const budget = await apiCall(`/budgets/${currentBudgetId}/categories`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        renderBudget(budget);
        e.target.reset();
        clearAlerts();
    } catch (err) {
        alert('Failed to add category: ' + err.message);
    }
});

// ==================== Validate ====================
document.getElementById('btn-validate').addEventListener('click', async () => {
    try {
        const alerts = await apiCall(`/budgets/${currentBudgetId}/validate`, { method: 'POST' });
        renderAlerts(alerts);
        // Re-fetch to reflect any DRAFT -> READY transition in the badge.
        const budget = await apiCall(`/budgets/${currentBudgetId}`);
        renderBudget(budget);
    } catch (err) {
        alert('Validation failed: ' + err.message);
    }
});

// ==================== Submit ====================
document.getElementById('btn-submit').addEventListener('click', async () => {
    clearDecision();
    try {
        const decision = await apiCall(`/budgets/${currentBudgetId}/submit`, { method: 'POST' });
        renderDecision(decision);
        const budget = await apiCall(`/budgets/${currentBudgetId}`);
        renderBudget(budget);
    } catch (err) {
        renderSubmitBlocked(err.message);
    }
});

// ==================== Reset ====================
document.getElementById('btn-reset').addEventListener('click', () => {
    currentBudgetId = null;
    document.getElementById('workspace').classList.add('hidden');
    document.getElementById('step-create').classList.remove('hidden');
    document.getElementById('create-event-form').reset();
    clearAlerts();
});

// ==================== Alerts & decision rendering ====================
function renderAlerts(alerts) {
    const area = document.getElementById('alerts-area');
    if (!alerts || alerts.length === 0) {
        area.innerHTML = '<div class="alert alert-empty"><strong>All clear</strong> &mdash; no variance alerts, budget is valid</div>';
        return;
    }
    area.innerHTML = alerts
        .map(a => `<div class="alert ${a.severity}">
            <div><strong>${a.severity}</strong> &mdash; ${escapeHtml(a.message)}</div>
            <div class="code">${escapeHtml(a.code)}</div>
        </div>`)
        .join('');
}

function clearAlerts() {
    document.getElementById('alerts-area').innerHTML = '';
    document.getElementById('decision-area').innerHTML = '';
}

function clearDecision() {
    document.getElementById('decision-area').innerHTML = '';
}

function renderDecision(decision) {
    const area = document.getElementById('decision-area');
    const outcomeLabel = decision.outcome.replace(/_/g, ' ');
    area.innerHTML = `<div class="decision ${decision.outcome}">
        <h4>${outcomeLabel}</h4>
        <p>${escapeHtml(decision.reason)}</p>
        <span class="code">Strategy: ${escapeHtml(decision.strategyName)}</span>
    </div>`;
}

function renderSubmitBlocked(message) {
    const area = document.getElementById('decision-area');
    area.innerHTML = `<div class="decision REJECTED">
        <h4>Submission Blocked</h4>
        <p>${escapeHtml(message)}</p>
    </div>`;
}

// ==================== Init ====================
loadOrganizers();
