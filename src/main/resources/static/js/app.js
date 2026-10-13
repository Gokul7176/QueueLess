/**
 * QueueLess — Virtual Queue Management System
 * Frontend Application Controller (Vanilla ES6 JavaScript)
 * College DevOps Laboratory Project — 23IT723
 */

(function () {
    'use strict';

    // Application State
    const state = {
        activeTab: 'customer-view',
        activeTicketNumber: localStorage.getItem('queueless_active_ticket') || null,
        autoRefreshInterval: null,
        isAutoRefreshActive: true,
        currentFilter: 'ALL',
        isOnline: true,
        versionFilter: 'ALL',
        versionSearchQuery: '',
        releases: [],
        currentRelease: null
    };

    // DOM Elements
    const elements = {
        // Clock & Status
        clockDisplay: document.getElementById('clockDisplay'),
        serverStatusBadge: document.getElementById('serverStatusBadge'),
        serverStatusText: document.getElementById('serverStatusText'),
        toastContainer: document.getElementById('toastContainer'),
        systemAlert: document.getElementById('systemAlert'),
        footerBuildBadge: document.getElementById('footerBuildBadge'),

        // Tab Navigation
        tabButtons: document.querySelectorAll('.tab-btn'),
        viewPanels: document.querySelectorAll('.view-panel'),

        // Customer View
        joinQueueForm: document.getElementById('joinQueueForm'),
        customerNameInput: document.getElementById('customerNameInput'),
        serviceTypeSelect: document.getElementById('serviceTypeSelect'),
        btnJoinQueue: document.getElementById('btnJoinQueue'),
        nameError: document.getElementById('nameError'),

        searchTicketInput: document.getElementById('searchTicketInput'),
        btnSearchTicket: document.getElementById('btnSearchTicket'),
        activeTicketDisplay: document.getElementById('activeTicketDisplay'),
        ticketEmptyState: document.getElementById('ticketEmptyState'),
        ticketNumberDisplay: document.getElementById('ticketNumberDisplay'),
        ticketStatusBadge: document.getElementById('ticketStatusBadge'),
        ticketCustomerDisplay: document.getElementById('ticketCustomerDisplay'),
        ticketServiceDisplay: document.getElementById('ticketServiceDisplay'),
        ticketPositionDisplay: document.getElementById('ticketPositionDisplay'),
        ticketWaitDisplay: document.getElementById('ticketWaitDisplay'),
        ticketCounterDisplay: document.getElementById('ticketCounterDisplay'),
        ticketJoinedTimeDisplay: document.getElementById('ticketJoinedTimeDisplay'),
        btnCancelMyTicket: document.getElementById('btnCancelMyTicket'),

        // Admin View
        kpiWaiting: document.getElementById('kpiWaiting'),
        kpiServing: document.getElementById('kpiServing'),
        kpiCompleted: document.getElementById('kpiCompleted'),
        kpiTotal: document.getElementById('kpiTotal'),
        adminCounterSelect: document.getElementById('adminCounterSelect'),
        btnCallNext: document.getElementById('btnCallNext'),
        btnManualRefresh: document.getElementById('btnManualRefresh'),
        btnResetQueue: document.getElementById('btnResetQueue'),
        autoRefreshToggle: document.getElementById('autoRefreshToggle'),
        filterPills: document.querySelectorAll('#filterPills .pill'),
        ticketsTableBody: document.getElementById('ticketsTableBody'),

        // Live Board View
        servingCountersContainer: document.getElementById('servingCountersContainer'),
        upNextListContainer: document.getElementById('upNextListContainer'),

        // Version History View
        currentVersionBadge: document.getElementById('currentVersionBadge'),
        currentDockerTag: document.getElementById('currentDockerTag'),
        currentCommitHash: document.getElementById('currentCommitHash'),
        currentBranchLabel: document.getElementById('currentBranchLabel'),
        currentCommitMsg: document.getElementById('currentCommitMsg'),
        currentQualityGate: document.getElementById('currentQualityGate'),
        currentJenkinsBuild: document.getElementById('currentJenkinsBuild'),
        totalReleasesCount: document.getElementById('totalReleasesCount'),
        currentEnvBadge: document.getElementById('currentEnvBadge'),
        versionSearchInput: document.getElementById('versionSearchInput'),
        versionFilterPills: document.querySelectorAll('#versionFilterPills .pill'),
        versionTableBody: document.getElementById('versionTableBody'),
        versionEmptyState: document.getElementById('versionEmptyState'),
        versionEmptyStateMsg: document.getElementById('versionEmptyStateMsg'),
        btnRefreshVersions: document.getElementById('btnRefreshVersions')
    };

    // =========================================================================
    // INITIALIZATION & LIFECYCLE
    // =========================================================================
    function init() {
        startClock();
        bindEvents();
        setupAutoRefresh();

        // Load active customer ticket if present
        if (state.activeTicketNumber) {
            elements.searchTicketInput.value = state.activeTicketNumber;
            fetchTicketStatus(state.activeTicketNumber, false);
        }

        // Initial Data Load
        refreshDashboardData();
        fetchCurrentVersionInfo();
    }

    // Live Clock Display
    function startClock() {
        function updateClock() {
            const now = new Date();
            elements.clockDisplay.textContent = now.toLocaleTimeString([], {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        }
        updateClock();
        setInterval(updateClock, 1000);
    }

    // Auto-Refresh Polling (5 seconds)
    function setupAutoRefresh() {
        if (state.autoRefreshInterval) {
            clearInterval(state.autoRefreshInterval);
        }

        if (state.isAutoRefreshActive) {
            state.autoRefreshInterval = setInterval(() => {
                refreshDashboardData(true);
                if (state.activeTicketNumber) {
                    fetchTicketStatus(state.activeTicketNumber, false);
                }
                if (state.activeTab === 'version-view') {
                    loadVersionHistoryData(false);
                }
            }, 5000);
        }
    }

    // =========================================================================
    // EVENT BINDINGS
    // =========================================================================
    function bindEvents() {
        // Tab Switcher
        elements.tabButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetView = btn.getAttribute('data-tab');
                switchTab(targetView);
            });
        });

        // Join Queue Form
        elements.joinQueueForm.addEventListener('submit', handleJoinQueue);

        // Search Ticket
        elements.btnSearchTicket.addEventListener('click', () => {
            const ticketNo = elements.searchTicketInput.value.trim().toUpperCase();
            if (ticketNo) {
                fetchTicketStatus(ticketNo, true);
            } else {
                showToast('Please enter a ticket number to search', 'error');
            }
        });

        elements.searchTicketInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                elements.btnSearchTicket.click();
            }
        });

        // Cancel My Ticket
        elements.btnCancelMyTicket.addEventListener('click', handleCancelMyTicket);

        // Admin Controls
        elements.btnCallNext.addEventListener('click', handleCallNext);
        elements.btnManualRefresh.addEventListener('click', () => {
            refreshDashboardData(false);
            showToast('Queue data refreshed', 'info');
        });

        elements.btnResetQueue.addEventListener('click', handleResetQueue);

        elements.autoRefreshToggle.addEventListener('change', (e) => {
            state.isAutoRefreshActive = e.target.checked;
            setupAutoRefresh();
            showToast(state.isAutoRefreshActive ? 'Auto-refresh enabled (5s)' : 'Auto-refresh paused', 'info');
        });

        // Filter Pills
        elements.filterPills.forEach(pill => {
            pill.addEventListener('click', () => {
                elements.filterPills.forEach(p => p.classList.remove('active'));
                pill.classList.add('active');
                state.currentFilter = pill.getAttribute('data-filter');
                loadTicketsTable();
            });
        });

        // Version History Filter & Search Events
        if (elements.versionSearchInput) {
            elements.versionSearchInput.addEventListener('input', (e) => {
                state.versionSearchQuery = e.target.value.toLowerCase().trim();
                renderVersionTable();
            });
        }

        if (elements.versionFilterPills) {
            elements.versionFilterPills.forEach(pill => {
                pill.addEventListener('click', () => {
                    elements.versionFilterPills.forEach(p => p.classList.remove('active'));
                    pill.classList.add('active');
                    state.versionFilter = pill.getAttribute('data-vfilter') || 'ALL';
                    renderVersionTable();
                });
            });
        }

        if (elements.btnRefreshVersions) {
            elements.btnRefreshVersions.addEventListener('click', () => {
                loadVersionHistoryData(true);
            });
        }
    }

    function switchTab(viewId) {
        state.activeTab = viewId;
        elements.tabButtons.forEach(btn => {
            const matches = btn.getAttribute('data-tab') === viewId;
            btn.classList.toggle('active', matches);
            btn.setAttribute('aria-selected', matches ? 'true' : 'false');
        });

        elements.viewPanels.forEach(panel => {
            panel.classList.toggle('active', panel.id === viewId);
        });

        // Refresh view-specific content
        if (viewId === 'admin-view' || viewId === 'display-view') {
            refreshDashboardData(false);
        } else if (viewId === 'version-view') {
            loadVersionHistoryData(false);
        }
    }

    // =========================================================================
    // API OPERATIONS
    // =========================================================================

    // Join Queue Handler
    async function handleJoinQueue(e) {
        e.preventDefault();
        const name = elements.customerNameInput.value.trim();
        const service = elements.serviceTypeSelect.value;

        // Validation
        if (!name || name.length < 2) {
            elements.nameError.textContent = 'Name must be at least 2 characters long.';
            elements.nameError.style.display = 'block';
            elements.customerNameInput.focus();
            return;
        }
        elements.nameError.style.display = 'none';

        setButtonLoading(elements.btnJoinQueue, true, 'Generating Ticket...');

        try {
            const response = await fetch('/api/tickets/join', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ customerName: name, serviceType: service })
            });

            const result = await response.json();

            if (response.ok && result.success) {
                const ticket = result.data;
                state.activeTicketNumber = ticket.ticketNumber;
                localStorage.setItem('queueless_active_ticket', ticket.ticketNumber);

                renderTicketPass(ticket);
                showToast(`Ticket ${ticket.ticketNumber} generated successfully!`, 'success');
                elements.customerNameInput.value = '';

                // Refresh table & board
                refreshDashboardData(true);
            } else {
                showToast(result.message || 'Unable to join queue', 'error');
            }
        } catch (error) {
            console.error('Error joining queue:', error);
            showToast('Server connection error. Please ensure Spring Boot is running.', 'error');
            setServerOnlineStatus(false);
        } finally {
            setButtonLoading(elements.btnJoinQueue, false, 'Generate Queue Ticket');
        }
    }

    // Fetch Ticket Status
    async function fetchTicketStatus(ticketNumber, notifyIfFound = false) {
        try {
            const response = await fetch(`/api/tickets/${encodeURIComponent(ticketNumber)}`);
            const result = await response.json();

            if (response.ok && result.success) {
                setServerOnlineStatus(true);
                const ticket = result.data;
                renderTicketPass(ticket);
                if (notifyIfFound) {
                    showToast(`Loaded ticket ${ticket.ticketNumber}`, 'info');
                }
            } else {
                elements.activeTicketDisplay.classList.add('hidden');
                elements.ticketEmptyState.classList.remove('hidden');
                if (notifyIfFound) {
                    showToast(`Ticket '${ticketNumber}' not found.`, 'error');
                }
            }
        } catch (error) {
            console.error('Error fetching ticket status:', error);
            setServerOnlineStatus(false);
        }
    }

    // Cancel My Active Ticket
    async function handleCancelMyTicket() {
        if (!state.activeTicketNumber) return;

        if (!confirm(`Are you sure you want to cancel ticket ${state.activeTicketNumber}?`)) {
            return;
        }

        try {
            const response = await fetch(`/api/tickets/${encodeURIComponent(state.activeTicketNumber)}/cancel`, {
                method: 'POST'
            });
            const result = await response.json();

            if (response.ok && result.success) {
                showToast(`Ticket ${state.activeTicketNumber} cancelled`, 'info');
                renderTicketPass(result.data);
                refreshDashboardData(true);
            } else {
                showToast(result.message || 'Failed to cancel ticket', 'error');
            }
        } catch (error) {
            console.error('Error cancelling ticket:', error);
            showToast('Network error while cancelling ticket', 'error');
        }
    }

    // Admin: Call Next Customer
    async function handleCallNext() {
        const counter = elements.adminCounterSelect.value;
        setButtonLoading(elements.btnCallNext, true, 'Calling...');

        try {
            const response = await fetch('/api/tickets/call-next', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ counterName: counter })
            });

            const result = await response.json();

            if (response.ok && result.success) {
                const calledTicket = result.data;
                showToast(`📢 Called Ticket ${calledTicket.ticketNumber} to ${counter}!`, 'success');
                refreshDashboardData(false);

                // If this happens to be user's ticket, update their pass
                if (state.activeTicketNumber === calledTicket.ticketNumber) {
                    renderTicketPass(calledTicket);
                }
            } else {
                showToast(result.message || 'No waiting customers in queue.', 'error');
            }
        } catch (error) {
            console.error('Error calling next ticket:', error);
            showToast('Failed to call next customer', 'error');
        } finally {
            setButtonLoading(elements.btnCallNext, false, '📢 Call Next Waiting Customer');
        }
    }

    // Admin: Mark Ticket Completed
    async function handleCompleteTicket(ticketNumber) {
        try {
            const response = await fetch(`/api/tickets/${encodeURIComponent(ticketNumber)}/complete`, {
                method: 'POST'
            });
            const result = await response.json();

            if (response.ok && result.success) {
                showToast(`Ticket ${ticketNumber} completed!`, 'success');
                refreshDashboardData(true);

                if (state.activeTicketNumber === ticketNumber) {
                    renderTicketPass(result.data);
                }
            } else {
                showToast(result.message || 'Failed to complete ticket', 'error');
            }
        } catch (error) {
            console.error('Error completing ticket:', error);
            showToast('Network error while completing ticket', 'error');
        }
    }

    // Admin: Cancel Ticket from Table
    async function handleAdminCancel(ticketNumber) {
        if (!confirm(`Cancel ticket ${ticketNumber}?`)) return;

        try {
            const response = await fetch(`/api/tickets/${encodeURIComponent(ticketNumber)}/cancel`, {
                method: 'POST'
            });
            const result = await response.json();

            if (response.ok && result.success) {
                showToast(`Ticket ${ticketNumber} cancelled`, 'info');
                refreshDashboardData(true);
            } else {
                showToast(result.message || 'Failed to cancel ticket', 'error');
            }
        } catch (error) {
            console.error('Error cancelling ticket:', error);
            showToast('Network error', 'error');
        }
    }

    // Admin: Reset Queue
    async function handleResetQueue() {
        if (!confirm('⚠️ Are you sure you want to RESET the entire virtual queue? All active and completed tickets will be purged for laboratory testing.')) {
            return;
        }

        try {
            const response = await fetch('/api/tickets/reset', { method: 'POST' });
            const result = await response.json();

            if (response.ok && result.success) {
                showToast('Queue has been completely reset.', 'info');
                localStorage.removeItem('queueless_active_ticket');
                state.activeTicketNumber = null;
                elements.activeTicketDisplay.classList.add('hidden');
                elements.ticketEmptyState.classList.remove('hidden');
                refreshDashboardData(false);
            } else {
                showToast('Failed to reset queue', 'error');
            }
        } catch (error) {
            console.error('Error resetting queue:', error);
            showToast('Network error while resetting queue', 'error');
        }
    }

    // =========================================================================
    // DATA REFRESH & RENDER FUNCTIONS
    // =========================================================================

    async function refreshDashboardData(isBackground = false) {
        try {
            await Promise.all([
                loadQueueStats(),
                loadTicketsTable(),
                loadLiveDisplayBoard()
            ]);
            setServerOnlineStatus(true);
        } catch (error) {
            console.error('Dashboard refresh failed:', error);
            setServerOnlineStatus(false);
        }
    }

    // Load Stats KPIs
    async function loadQueueStats() {
        const response = await fetch('/api/tickets/stats');
        const result = await response.json();

        if (response.ok && result.success) {
            const stats = result.data;
            elements.kpiWaiting.textContent = stats.waitingCount;
            elements.kpiServing.textContent = stats.servingCount;
            elements.kpiCompleted.textContent = stats.completedCount;
            elements.kpiTotal.textContent = stats.totalCount;
        }
    }

    // Load Tickets Table
    async function loadTicketsTable() {
        let url = '/api/tickets';
        if (state.currentFilter && state.currentFilter !== 'ALL') {
            url += `?status=${encodeURIComponent(state.currentFilter)}`;
        }

        const response = await fetch(url);
        const result = await response.json();

        if (response.ok && result.success) {
            renderTicketsTable(result.data);
        }
    }

    function renderTicketsTable(tickets) {
        if (!tickets || tickets.length === 0) {
            elements.ticketsTableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center loading-cell">
                        No tickets found matching current criteria.
                    </td>
                </tr>
            `;
            return;
        }

        const rowsHtml = tickets.map(t => {
            const statusClass = `badge-${t.status.toLowerCase()}`;
            const positionText = t.queuePosition !== null ? (t.queuePosition === 0 ? 'Now Serving' : `# ${t.queuePosition}`) : '—';
            const counterText = t.counterName ? t.counterName : '—';
            const formattedTime = formatTime(t.createdAt);

            // Action buttons
            let actionButtons = '';
            if (t.status === 'SERVING') {
                actionButtons = `
                    <button class="btn btn-success btn-sm btn-complete-action" data-ticket="${t.ticketNumber}">
                        Complete
                    </button>
                `;
            } else if (t.status === 'WAITING') {
                const callBtn = t.queuePosition === 1 ? `
                    <button class="btn btn-primary btn-sm btn-call-eligible-action" data-ticket="${t.ticketNumber}" title="Call next waiting customer immediately (FIFO)">
                        📢 Call Next
                    </button>
                ` : '';
                actionButtons = `
                    <div style="display:inline-flex; gap:0.35rem; align-items:center;">
                        ${callBtn}
                        <button class="btn btn-outline-danger btn-sm btn-cancel-action" data-ticket="${t.ticketNumber}">
                            Cancel
                        </button>
                    </div>
                `;
            } else {
                actionButtons = `<span style="font-size:0.75rem; color:var(--text-subtle);">Done</span>`;
            }

            return `
                <tr>
                    <td><span class="ticket-tag">${escapeHtml(t.ticketNumber)}</span></td>
                    <td><strong>${escapeHtml(t.customerName)}</strong></td>
                    <td>${escapeHtml(t.serviceType)}</td>
                    <td><span class="badge ${statusClass}">${t.status}</span></td>
                    <td><strong>${positionText}</strong></td>
                    <td>${escapeHtml(counterText)}</td>
                    <td>${formattedTime}</td>
                    <td class="text-right">${actionButtons}</td>
                </tr>
            `;
        }).join('');

        elements.ticketsTableBody.innerHTML = rowsHtml;

        // Attach action handlers
        elements.ticketsTableBody.querySelectorAll('.btn-complete-action').forEach(btn => {
            btn.addEventListener('click', () => {
                const ticketNo = btn.getAttribute('data-ticket');
                handleCompleteTicket(ticketNo);
            });
        });

        elements.ticketsTableBody.querySelectorAll('.btn-call-eligible-action').forEach(btn => {
            btn.addEventListener('click', () => {
                handleCallNext();
            });
        });

        elements.ticketsTableBody.querySelectorAll('.btn-cancel-action').forEach(btn => {
            btn.addEventListener('click', () => {
                const ticketNo = btn.getAttribute('data-ticket');
                handleAdminCancel(ticketNo);
            });
        });
    }

    // Load Live Public Display Board
    async function loadLiveDisplayBoard() {
        const response = await fetch('/api/tickets');
        const result = await response.json();

        if (!response.ok || !result.success) return;

        const allTickets = result.data;
        const servingTickets = allTickets.filter(t => t.status === 'SERVING');
        const waitingTickets = allTickets.filter(t => t.status === 'WAITING').slice(0, 6);

        // Render Serving Counters
        if (servingTickets.length === 0) {
            elements.servingCountersContainer.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1; padding: 2rem;">
                    <div class="empty-icon">🛋️</div>
                    <h4>No Counters Currently Serving</h4>
                    <p>When an administrator calls a customer, the live counter card appears here.</p>
                </div>
            `;
        } else {
            elements.servingCountersContainer.innerHTML = servingTickets.map(t => `
                <div class="counter-display-card">
                    <div class="counter-title">🎯 ${escapeHtml(t.counterName || 'Service Desk')}</div>
                    <div class="counter-ticket">${escapeHtml(t.ticketNumber)}</div>
                    <div class="counter-name">${escapeHtml(t.customerName)}</div>
                    <div class="counter-dept">${escapeHtml(t.serviceType)}</div>
                </div>
            `).join('');
        }

        // Render Up Next
        if (waitingTickets.length === 0) {
            elements.upNextListContainer.innerHTML = `
                <div class="empty-state" style="padding: 2rem;">
                    <div class="empty-icon">✨</div>
                    <h4>Waiting Queue is Clear</h4>
                    <p>New arrivals will automatically populate this real-time queue.</p>
                </div>
            `;
        } else {
            elements.upNextListContainer.innerHTML = waitingTickets.map(t => `
                <div class="up-next-item">
                    <div>
                        <div class="item-ticket">${escapeHtml(t.ticketNumber)}</div>
                        <div class="item-name">${escapeHtml(t.customerName)}</div>
                    </div>
                    <div class="item-details">
                        <span class="badge badge-waiting">Wait ~${t.estimatedWaitMinutes || 5}m</span>
                        <span class="item-pos">Position #${t.queuePosition || 1}</span>
                    </div>
                </div>
            `).join('');
        }
    }

    // Render Ticket Pass (Customer View)
    function renderTicketPass(ticket) {
        elements.ticketEmptyState.classList.add('hidden');
        elements.activeTicketDisplay.classList.remove('hidden');

        elements.ticketNumberDisplay.textContent = ticket.ticketNumber;
        elements.ticketCustomerDisplay.textContent = ticket.customerName;
        elements.ticketServiceDisplay.textContent = ticket.serviceType;

        // Badge
        elements.ticketStatusBadge.textContent = ticket.status;
        elements.ticketStatusBadge.className = 'pass-badge ' + ticket.status.toLowerCase();

        // Metrics
        if (ticket.status === 'WAITING') {
            elements.ticketPositionDisplay.textContent = `# ${ticket.queuePosition || 1}`;
            elements.ticketWaitDisplay.textContent = `~${ticket.estimatedWaitMinutes || 5} mins`;
            elements.ticketCounterDisplay.textContent = 'In Line';
            elements.btnCancelMyTicket.classList.remove('hidden');
        } else if (ticket.status === 'SERVING') {
            elements.ticketPositionDisplay.textContent = 'Now!';
            elements.ticketWaitDisplay.textContent = 'Serving';
            elements.ticketCounterDisplay.textContent = ticket.counterName || 'Counter 1';
            elements.btnCancelMyTicket.classList.add('hidden');
        } else if (ticket.status === 'COMPLETED') {
            elements.ticketPositionDisplay.textContent = 'Completed';
            elements.ticketWaitDisplay.textContent = 'Done';
            elements.ticketCounterDisplay.textContent = ticket.counterName || 'Counter';
            elements.btnCancelMyTicket.classList.add('hidden');
        } else {
            elements.ticketPositionDisplay.textContent = 'Cancelled';
            elements.ticketWaitDisplay.textContent = 'N/A';
            elements.ticketCounterDisplay.textContent = 'N/A';
            elements.btnCancelMyTicket.classList.add('hidden');
        }

        elements.ticketJoinedTimeDisplay.textContent = `Joined: ${formatTime(ticket.createdAt)}`;
    }

    // =========================================================================
    // UTILITIES
    // =========================================================================

    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
        toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;

        elements.toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.25s ease';
            setTimeout(() => toast.remove(), 250);
        }, 4000);
    }

    function setButtonLoading(button, isLoading, text) {
        if (isLoading) {
            button.disabled = true;
            button.dataset.originalText = button.innerHTML;
            button.innerHTML = `<span class="status-dot"></span> ${text}`;
        } else {
            button.disabled = false;
            if (button.dataset.originalText) {
                button.innerHTML = button.dataset.originalText;
            } else {
                button.textContent = text;
            }
        }
    }

    function setServerOnlineStatus(online) {
        state.isOnline = online;
        if (online) {
            elements.serverStatusBadge.className = 'status-pill online';
            elements.serverStatusText.textContent = 'System Online';
        } else {
            elements.serverStatusBadge.className = 'status-pill';
            elements.serverStatusBadge.style.backgroundColor = 'var(--danger-light)';
            elements.serverStatusBadge.style.color = 'var(--danger)';
            elements.serverStatusBadge.style.border = '1px solid #fecaca';
            elements.serverStatusText.textContent = 'Server Offline';
        }
    }

    // =========================================================================
    // VERSION HISTORY & RELEASE MONITOR API & RENDERING
    // =========================================================================

    async function fetchCurrentVersionInfo() {
        try {
            const response = await fetch('/api/version-history/current');
            const result = await response.json();
            if (response.ok && result.success && result.data) {
                const cur = result.data;
                state.currentRelease = cur;

                if (elements.currentVersionBadge) {
                    elements.currentVersionBadge.textContent = 'v' + (cur.version || '1.1.0').replace(/^v/, '');
                }
                if (elements.currentDockerTag) {
                    elements.currentDockerTag.textContent = cur.dockerImageTag || 'queueless:latest';
                }
                if (elements.currentCommitHash) {
                    elements.currentCommitHash.textContent = cur.gitCommitShort || 'c038603';
                }
                if (elements.currentBranchLabel) {
                    elements.currentBranchLabel.textContent = 'Branch: ' + (cur.gitBranch || 'main');
                }
                if (elements.currentCommitMsg) {
                    elements.currentCommitMsg.textContent = cur.commitMessage || 'feat: release v1.1.0';
                    elements.currentCommitMsg.title = cur.commitMessage || '';
                }
                if (elements.currentQualityGate) {
                    const tp = (cur.testResult || '').toUpperCase();
                    elements.currentQualityGate.textContent = tp.includes('PASSED') ? 'PASSED' : (cur.testResult || 'VERIFIED');
                }
                if (elements.currentJenkinsBuild) {
                    elements.currentJenkinsBuild.textContent = cur.jenkinsBuildNumber ? ('Jenkins Build #' + cur.jenkinsBuildNumber) : 'Jenkins CI/CD';
                }
                if (elements.currentEnvBadge) {
                    elements.currentEnvBadge.textContent = 'Env: ' + (cur.environment || 'Production');
                }
                if (elements.footerBuildBadge) {
                    elements.footerBuildBadge.textContent = `● Build v${(cur.version || '1.1.0').replace(/^v/, '')} Verified`;
                }
            }
        } catch (error) {
            console.error('Error fetching current version info:', error);
        }
    }

    async function loadVersionHistoryData(notify = false) {
        try {
            await fetchCurrentVersionInfo();
            const response = await fetch('/api/version-history');
            const result = await response.json();

            if (response.ok && result.success && Array.isArray(result.data)) {
                state.releases = result.data;
                if (elements.totalReleasesCount) {
                    elements.totalReleasesCount.textContent = state.releases.length;
                }
                renderVersionTable();
                if (notify) {
                    showToast(`Release history updated (${state.releases.length} records)`, 'info');
                }
            } else {
                if (elements.versionTableBody) {
                    elements.versionTableBody.innerHTML = '<tr><td colspan="9" class="text-center text-muted">Unable to load release history records.</td></tr>';
                }
            }
        } catch (error) {
            console.error('Error loading version history:', error);
            if (elements.versionTableBody) {
                elements.versionTableBody.innerHTML = '<tr><td colspan="9" class="text-center text-danger">Failed to connect to version history API.</td></tr>';
            }
        }
    }

    function renderVersionTable() {
        if (!elements.versionTableBody) return;

        let filtered = state.releases || [];

        // Apply Status Filter
        if (state.versionFilter && state.versionFilter !== 'ALL') {
            filtered = filtered.filter(r => {
                const s = (r.deploymentStatus || '').toUpperCase();
                return s === state.versionFilter.toUpperCase();
            });
        }

        // Apply Search Query
        if (state.versionSearchQuery) {
            const q = state.versionSearchQuery;
            filtered = filtered.filter(r =>
                (r.version && r.version.toLowerCase().includes(q)) ||
                (r.gitCommit && r.gitCommit.toLowerCase().includes(q)) ||
                (r.gitCommitShort && r.gitCommitShort.toLowerCase().includes(q)) ||
                (r.gitBranch && r.gitBranch.toLowerCase().includes(q)) ||
                (r.commitMessage && r.commitMessage.toLowerCase().includes(q)) ||
                (r.jenkinsBuildNumber && String(r.jenkinsBuildNumber).toLowerCase().includes(q)) ||
                (r.environment && r.environment.toLowerCase().includes(q)) ||
                (r.dockerImageTag && r.dockerImageTag.toLowerCase().includes(q))
            );
        }

        if (filtered.length === 0) {
            elements.versionTableBody.innerHTML = '';
            if (elements.versionEmptyState) {
                elements.versionEmptyState.classList.remove('hidden');
                if (elements.versionEmptyStateMsg) {
                    elements.versionEmptyStateMsg.textContent = state.releases.length === 0
                        ? 'No release history records have been recorded yet. Releases deployed through Jenkins CI/CD pipeline will automatically appear here.'
                        : 'No releases match your current search and filter criteria. Try clearing the filter or search query.';
                }
            }
            return;
        }

        if (elements.versionEmptyState) {
            elements.versionEmptyState.classList.add('hidden');
        }

        elements.versionTableBody.innerHTML = filtered.map(r => {
            const isCurr = r.current === true;
            const statusUpper = (r.deploymentStatus || 'DEPLOYED').toUpperCase();
            let statusBadgeClass = 'status-badge-deployed';
            let statusIcon = '🚀';

            if (statusUpper === 'VERIFIED') {
                statusBadgeClass = 'status-badge-verified';
                statusIcon = '✅';
            } else if (statusUpper === 'FAILED') {
                statusBadgeClass = 'status-badge-failed';
                statusIcon = '❌';
            }

            const testPassed = (r.testResult || '').toUpperCase().includes('PASSED');
            const testBadgeClass = testPassed ? 'test-badge-passed' : 'test-badge-failed';

            const jenkinsDisplay = r.jenkinsBuildUrl
                ? `<a href="${escapeHtml(r.jenkinsBuildUrl)}" target="_blank" rel="noopener noreferrer" class="jenkins-link">
                    🔗 #${escapeHtml(r.jenkinsBuildNumber || 'Job')}
                   </a>`
                : (r.jenkinsBuildNumber ? `#${escapeHtml(r.jenkinsBuildNumber)}` : 'Manual');

            const formattedDate = r.deployedAt ? formatTimestamp(r.deployedAt) : 'Recently';

            return `
                <tr>
                    <td>
                        <div class="version-tag-wrapper">
                            <span class="badge-version">v${escapeHtml(r.version || '1.0.0')}</span>
                            ${isCurr ? '<span class="badge-current">CURRENT</span>' : ''}
                        </div>
                    </td>
                    <td>
                        <div class="commit-cell">
                            <span class="commit-hash-pill" title="Full commit: ${escapeHtml(r.gitCommit || '')}">
                                ${escapeHtml(r.gitCommitShort || (r.gitCommit ? r.gitCommit.substring(0, 7) : ''))}
                            </span>
                        </div>
                    </td>
                    <td>
                        <span class="branch-pill">🌿 ${escapeHtml(r.gitBranch || 'main')}</span>
                    </td>
                    <td style="max-width: 240px;" title="${escapeHtml(r.commitMessage || '')}">
                        <div class="truncate" style="font-size: 0.825rem; color: var(--text-main);">
                            ${escapeHtml(r.commitMessage || 'Automated deployment build')}
                        </div>
                    </td>
                    <td>
                        ${jenkinsDisplay}
                    </td>
                    <td>
                        <span class="${testBadgeClass}">${escapeHtml(r.testResult || 'PASSED')}</span>
                    </td>
                    <td>
                        <span class="${statusBadgeClass}">
                            <span>${statusIcon}</span> ${escapeHtml(statusUpper)}
                        </span>
                    </td>
                    <td>
                        <div class="docker-tag-cell">
                            <span style="font-size: 0.75rem; color: #94a3b8;">${escapeHtml(r.environment || 'production')}</span>
                            <span class="docker-tag-pill truncate" title="${escapeHtml(r.dockerImageTag || '')}">
                                🐳 ${escapeHtml(r.dockerImageTag || 'queueless:latest')}
                            </span>
                        </div>
                    </td>
                    <td style="font-size: 0.775rem; color: var(--text-muted); white-space: nowrap;">
                        ${formattedDate}
                    </td>
                </tr>
            `;
        }).join('');
    }

    function formatTimestamp(isoString) {
        if (!isoString) return '—';
        try {
            const date = new Date(isoString);
            return date.toLocaleDateString([], {
                month: 'short',
                day: 'numeric',
                year: 'numeric'
            }) + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        } catch (e) {
            return isoString;
        }
    }

    function formatTime(isoString) {
        if (!isoString) return 'Just now';
        try {
            const date = new Date(isoString);
            return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        } catch (e) {
            return isoString;
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // Launch app once DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
