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
        tbody.innerHTML = '<tr class="empty-row"><td colspan="4">No categories yet &mdash; add one below</td></tr>';
    } else {
        tbody.innerHTML = budget.categories
            .map(c => `<tr>
                <td>${escapeHtml(c.name)}</td>
                <td class="right">${formatMoney(c.allocatedAmount)}</td>
                <td class="right">${formatMoney(c.spentAmount || 0)}</td>
                <td class="rule-cell">
                    <span class="rule-summary" id="rule-summary-${c.id}"></span>
                </td>
            </tr>`)
            .join('');
        budget.categories.forEach(c => refreshRuleSummary(c.id));
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

    // Expense submission section — only active once the budget is APPROVED.
    const expenseSection = document.getElementById('expense-section');
    if (budget.status === 'APPROVED') {
        expenseSection.classList.remove('hidden');
        populateExpenseCategorySelect(budget.categories);
        loadExpenses();
        loadRoutingPolicy();
    } else {
        expenseSection.classList.add('hidden');
    }
}

// ==================== Expense submission (UC #2) ====================
function populateExpenseCategorySelect(categories) {
    const select = document.getElementById('expense-category-select');
    if (!categories || categories.length === 0) {
        select.innerHTML = '<option value="">(no categories)</option>';
        return;
    }
    select.innerHTML = categories
        .map(c => `<option value="${c.id}">${escapeHtml(c.name)} &mdash; allocated ${formatMoney(c.allocatedAmount)}</option>`)
        .join('');
}

async function loadRoutingPolicy() {
    try {
        const { activeStrategy } = await apiCall('/expenses/routing-policy');
        document.getElementById('routing-policy-tag').textContent = `Routing policy: ${activeStrategy}`;
    } catch (err) {
        /* non-fatal */
    }
}

async function loadExpenses() {
    if (!currentBudgetId) return;
    try {
        const expenses = await apiCall(`/expenses?budgetId=${currentBudgetId}`);
        renderExpenses(expenses);
    } catch (err) {
        alert('Failed to load expenses: ' + err.message);
    }
}

function renderExpenses(expenses) {
    const tbody = document.querySelector('#expenses-table tbody');
    if (!expenses || expenses.length === 0) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="5">No expenses submitted yet</td></tr>';
        return;
    }
    tbody.innerHTML = expenses
        .map(e => `<tr>
            <td>${escapeHtml(e.description)}</td>
            <td>${escapeHtml(e.categoryName)}</td>
            <td class="right">${formatMoney(e.amount)}</td>
            <td><span class="level-badge ${e.requiredApprovalLevel}">${e.requiredApprovalLevel.replace(/_/g, ' ')}</span></td>
            <td><span class="expense-status ${e.status}">${e.status.replace(/_/g, ' ')}</span></td>
        </tr>`)
        .join('');
}

document.getElementById('submit-expense-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const organizerId = Number(document.getElementById('organizer-select').value);
    const payload = {
        categoryId: Number(fd.get('categoryId')),
        submittedById: organizerId,
        description: fd.get('description'),
        amount: Number(fd.get('amount')),
        expenseDate: fd.get('expenseDate'),
        supportingDocUrl: fd.get('supportingDocUrl') || null
    };
    try {
        await apiCall('/expenses', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        e.target.reset();
        loadExpenses();
    } catch (err) {
        alert('Failed to submit expense: ' + err.message);
    }
});

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
    document.getElementById('rule-modal').classList.add('hidden');
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

// ==================== View switching (Organizer vs Approver) ====================
document.getElementById('mode-organizer').addEventListener('click', () => switchMode('organizer'));
document.getElementById('mode-approver').addEventListener('click', () => switchMode('approver'));

function switchMode(mode) {
    const isOrg = mode === 'organizer';
    document.getElementById('organizer-view').classList.toggle('hidden', !isOrg);
    document.getElementById('approver-view').classList.toggle('hidden', isOrg);
    document.getElementById('mode-organizer').classList.toggle('active', isOrg);
    document.getElementById('mode-approver').classList.toggle('active', !isOrg);
    if (!isOrg) {
        loadApprovers();
        loadPendingBudgets();
        loadPendingExpenses();
        loadRulesManagement();
    } else if (currentBudgetId) {
        // Refresh organizer view so status changes made from approver side are reflected.
        apiCall(`/budgets/${currentBudgetId}`).then(renderBudget).catch(() => {});
    }
}

async function loadPendingBudgets() {
    try {
        const pending = await apiCall('/budgets/pending-approval');
        const list = document.getElementById('pending-budgets-list');
        if (pending.length === 0) {
            list.innerHTML = '<div class="empty-pending">No budgets awaiting approval.</div>';
            return;
        }
        list.innerHTML = pending.map(b => {
            const cats = (b.categories || []).map(c =>
                `<tr><td>${escapeHtml(c.name)}</td><td class="right">${formatMoney(c.allocatedAmount)}</td></tr>`).join('')
                || '<tr class="empty-row"><td colspan="2">No categories defined</td></tr>';
            return `<div class="pending-card">
                <div class="top">
                    <div>
                        <h4>${escapeHtml(b.eventName)}</h4>
                        <p class="meta">Budget #${b.id} &middot; allocated ${formatMoney(b.allocatedTotal)} of ${formatMoney(b.totalLimit)}</p>
                    </div>
                    <div class="amount">${formatMoney(b.totalLimit)}</div>
                </div>
                <table class="mini-table">
                    <thead><tr><th>Category</th><th class="right">Allocated</th></tr></thead>
                    <tbody>${cats}</tbody>
                </table>
                <div class="actions">
                    <button id="budget-approve-${b.id}" class="primary">Approve Budget</button>
                    <button id="budget-reject-${b.id}" class="btn-reject">Reject Budget</button>
                </div>
            </div>`;
        }).join('');
        pending.forEach(b => {
            document.getElementById(`budget-approve-${b.id}`).addEventListener('click', () => actOnBudget(b.id, 'manual-approve'));
            document.getElementById(`budget-reject-${b.id}`).addEventListener('click', () => actOnBudget(b.id, 'manual-reject'));
        });
    } catch (err) {
        /* non-fatal */
    }
}

async function actOnBudget(budgetId, action) {
    try {
        await apiCall(`/budgets/${budgetId}/${action}`, { method: 'POST' });
        showFeedback('ok', `Budget #${budgetId} ${action === 'manual-approve' ? 'approved' : 'rejected'}.`);
        loadPendingBudgets();
    } catch (err) {
        showFeedback('err', err.message);
    }
}

// ==================== Approver view (UC #3) ====================
async function loadApprovers() {
    try {
        const approvers = await apiCall('/approving-authorities');
        const select = document.getElementById('approver-select');
        if (approvers.length === 0) {
            select.innerHTML = '<option value="">(no approvers seeded)</option>';
            return;
        }
        select.innerHTML = approvers
            .map(a => `<option value="${a.id}">${escapeHtml(a.name)} &mdash; ${a.role} (limit ${formatMoney(a.approvalLimit)})</option>`)
            .join('');
    } catch (err) {
        alert('Failed to load approvers: ' + err.message);
    }
}

document.getElementById('btn-refresh-pending').addEventListener('click', loadPendingExpenses);

async function loadPendingExpenses() {
    try {
        const pending = await apiCall('/expenses/pending');
        const list = document.getElementById('pending-list');
        if (pending.length === 0) {
            list.innerHTML = '<div class="empty-pending">No expenses are currently awaiting approval.</div>';
            return;
        }
        // Fetch history for each so we can show "L1 approved" etc.
        const histories = await Promise.all(pending.map(e => apiCall(`/expenses/${e.id}/history`)));
        list.innerHTML = pending.map((e, i) => renderPendingCard(e, histories[i])).join('');
        pending.forEach(e => {
            document.getElementById(`approve-${e.id}`).addEventListener('click', () => actOnExpense(e.id, 'approve'));
            document.getElementById(`reject-${e.id}`).addEventListener('click', () => actOnExpense(e.id, 'reject'));
        });
    } catch (err) {
        alert('Failed to load pending expenses: ' + err.message);
    }
}

function renderPendingCard(e, history) {
    const historyHtml = history.length > 0
        ? `<div class="history">${history.map(h =>
            `<div class="history-item ${h.action}"><strong>${h.action}</strong> by ${escapeHtml(h.approverName)} (${h.approverRole})${h.notes ? ' &mdash; ' + escapeHtml(h.notes) : ''}</div>`).join('')}</div>`
        : '';
    return `<div class="pending-card">
        <div class="top">
            <div>
                <h4>${escapeHtml(e.description)}</h4>
                <p class="meta">Category: <strong>${escapeHtml(e.categoryName)}</strong> &middot; submitted by ${escapeHtml(e.submittedBy)} on ${e.expenseDate}</p>
                <div class="chip-row">
                    <span class="level-badge ${e.requiredApprovalLevel}">${e.requiredApprovalLevel.replace(/_/g, ' ')}</span>
                    <span class="expense-status ${e.status}">${e.status.replace(/_/g, ' ')}</span>
                </div>
            </div>
            <div class="amount">${formatMoney(e.amount)}</div>
        </div>
        ${historyHtml}
        <div class="actions">
            <button id="approve-${e.id}" class="primary">Approve</button>
            <button id="reject-${e.id}" class="btn-reject">Reject</button>
        </div>
    </div>`;
}

async function actOnExpense(expenseId, action) {
    const approverId = Number(document.getElementById('approver-select').value);
    if (!approverId) { showFeedback('err', 'Please pick an approver'); return; }
    try {
        if (action === 'approve') {
            const notes = prompt('Approval notes (optional):', '') || '';
            await apiCall(`/expenses/${expenseId}/approve`, {
                method: 'POST',
                body: JSON.stringify({ approverId, notes })
            });
            showFeedback('ok', `Expense #${expenseId} approved.`);
        } else {
            const reason = prompt('Reason for rejection (required):');
            if (!reason) return;
            await apiCall(`/expenses/${expenseId}/reject`, {
                method: 'POST',
                body: JSON.stringify({ approverId, reason })
            });
            showFeedback('ok', `Expense #${expenseId} rejected.`);
        }
        loadPendingExpenses();
    } catch (err) {
        showFeedback('err', err.message);
    }
}

function showFeedback(kind, msg) {
    const area = document.getElementById('approval-feedback');
    area.innerHTML = `<div class="feedback ${kind}">${escapeHtml(msg)}</div>`;
    setTimeout(() => { if (area.firstChild && area.firstChild.textContent === msg) area.innerHTML = ''; }, 5000);
}

// ==================== Rules management (finance-authority side) ====================
async function loadRulesManagement() {
    try {
        const budgets = await apiCall('/budgets');
        const list = document.getElementById('rules-management-list');
        const manageable = budgets.filter(b => b.status !== 'REJECTED');
        if (manageable.length === 0) {
            list.innerHTML = '<div class="empty-pending">No budgets with categories yet.</div>';
            return;
        }
        list.innerHTML = manageable.map(b => {
            const rows = (b.categories || []).map(c => `<tr>
                <td>${escapeHtml(c.name)}</td>
                <td class="right">${formatMoney(c.allocatedAmount)}</td>
                <td><span class="rule-summary" id="mgmt-rule-summary-${c.id}"></span></td>
                <td class="right"><button type="button" data-cat-id="${c.id}" data-cat-name="${escapeHtml(c.name)}" class="btn-edit-rule">Edit Rules</button></td>
            </tr>`).join('') || '<tr class="empty-row"><td colspan="4">No categories</td></tr>';
            return `<div class="rules-budget-block">
                <h4>${escapeHtml(b.eventName)} <span class="muted">&middot; Budget #${b.id} &middot; ${b.status}</span></h4>
                <table><thead><tr><th>Category</th><th class="right">Allocated</th><th>Current Rules</th><th class="right">&nbsp;</th></tr></thead><tbody>${rows}</tbody></table>
            </div>`;
        }).join('');
        document.querySelectorAll('#rules-management-list .btn-edit-rule').forEach(btn => {
            btn.addEventListener('click', () => openRuleModal(btn.dataset.catId, btn.dataset.catName));
        });
        manageable.forEach(b => (b.categories || []).forEach(c => refreshMgmtRuleSummary(c.id)));
    } catch (err) { /* non-fatal */ }
}

async function refreshMgmtRuleSummary(categoryId) {
    try {
        const rule = await apiCall(`/categories/${categoryId}/rule`);
        const el = document.getElementById(`mgmt-rule-summary-${categoryId}`);
        if (!el) return;
        const parts = [];
        if (rule.maxExpenseAmount) parts.push(`max ${formatMoney(rule.maxExpenseAmount)}`);
        if (rule.requiresL2ApprovalAbove) parts.push(`L2>${formatMoney(rule.requiresL2ApprovalAbove)}`);
        if (rule.blocked) { el.textContent = 'BLOCKED'; el.classList.add('blocked'); return; }
        el.classList.remove('blocked');
        el.textContent = parts.length ? parts.join(' · ') : 'no rules';
    } catch (err) { /* ignore */ }
}

// ==================== Category Rule modal (minor UC #3) ====================
async function refreshRuleSummary(categoryId) {
    try {
        const rule = await apiCall(`/categories/${categoryId}/rule`);
        const el = document.getElementById(`rule-summary-${categoryId}`);
        if (!el) return;
        const parts = [];
        if (rule.maxExpenseAmount) parts.push(`max ${formatMoney(rule.maxExpenseAmount)}`);
        if (rule.requiresL2ApprovalAbove) parts.push(`L2>${formatMoney(rule.requiresL2ApprovalAbove)}`);
        if (rule.blocked) { el.textContent = 'BLOCKED'; el.classList.add('blocked'); return; }
        el.classList.remove('blocked');
        el.textContent = parts.length ? parts.join(' · ') : 'no rules';
    } catch (err) { /* ignore */ }
}

function openRuleModal(categoryId, categoryName) {
    document.getElementById('rule-category-id').value = categoryId;
    document.getElementById('rule-modal-cat').textContent = '— ' + categoryName;
    apiCall(`/categories/${categoryId}/rule`).then(rule => {
        document.getElementById('rule-max').value = rule.maxExpenseAmount ?? '';
        document.getElementById('rule-l2-above').value = rule.requiresL2ApprovalAbove ?? '';
        document.getElementById('rule-blocked').checked = !!rule.blocked;
    });
    document.getElementById('rule-modal').classList.remove('hidden');
}

document.getElementById('rule-cancel').addEventListener('click', () => {
    document.getElementById('rule-modal').classList.add('hidden');
});

document.getElementById('rule-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('rule-category-id').value;
    const max = document.getElementById('rule-max').value;
    const l2 = document.getElementById('rule-l2-above').value;
    const payload = {
        maxExpenseAmount: max ? Number(max) : null,
        requiresL2ApprovalAbove: l2 ? Number(l2) : null,
        blocked: document.getElementById('rule-blocked').checked
    };
    try {
        await apiCall(`/categories/${id}/rule`, {
            method: 'PUT',
            body: JSON.stringify(payload)
        });
        document.getElementById('rule-modal').classList.add('hidden');
        refreshRuleSummary(id);
        refreshMgmtRuleSummary(id);
    } catch (err) {
        alert('Failed to save rule: ' + err.message);
    }
});

// ==================== Init ====================
loadOrganizers();
