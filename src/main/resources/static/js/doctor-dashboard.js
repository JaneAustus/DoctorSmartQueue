document.addEventListener('DOMContentLoaded', function () {
    const doctorSelect = document.getElementById('doctorSelect');
    const queueList = document.getElementById('queueList');
    const nextBtn = document.getElementById('nextPatientBtn');
    const completeBtn = document.getElementById('completeBtn');

    let currentEntry = null;
    let completedCount = 0;

    function getAuthHeaders() {
        const token = localStorage.getItem('fbIdToken');
        return {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        };
    }

    // Load initial doctor list
    loadDoctors();

    function loadDoctors() {
        fetch('/api/hospital/doctors', {
            headers: getAuthHeaders()
        })
            .then(res => res.json())
            .then(doctors => {
                doctorSelect.innerHTML = '';
                doctors.forEach(doc => {
                    const opt = document.createElement('option');
                    opt.value = doc.id;
                    opt.innerText = `${doc.name} (${doc.specialization})`;
                    doctorSelect.appendChild(opt);
                });
                refreshQueue();
            });
    }

    // Refresh when doctor changes
    doctorSelect.addEventListener('change', () => {
        currentEntry = null;
        updateUI();
        refreshQueue();
    });

    function refreshQueue() {
        const doctorId = doctorSelect.value;
        fetch(`/api/queue/doctor/${doctorId}`, {
            headers: getAuthHeaders()
        })
            .then(response => response.json())
            .then(data => {
                renderQueue(data);
                document.getElementById('waitingCount').innerText = data.length;
            })
            .catch(err => console.error('Error fetching queue:', err));
    }

    function renderQueue(data) {
        if (data.length === 0) {
            queueList.innerHTML = '<tr><td colspan="5" class="text-center py-5 text-muted">No patients in queue</td></tr>';
            return;
        }

        queueList.innerHTML = '';
        data.forEach((entry, index) => {
            const row = document.createElement('tr');
            row.className = 'patient-row';

            let priorityBadgeClass = 'bg-primary';
            if (entry.priority === 'EMERGENCY') priorityBadgeClass = 'bg-danger';
            if (entry.priority === 'ELDERLY') priorityBadgeClass = 'bg-warning text-dark';

            row.innerHTML = `
                <td class="ps-4">
                    <div class="fw-bold">#${entry.queueNumber}</div>
                    <small class="text-muted" style="font-size: 0.75rem;">${entry.tokenCode}</small>
                </td>
                <td>
                    <div class="fw-bold">${entry.patient.firstName} ${entry.patient.lastName}</div>
                    <small class="text-muted">${entry.patient.phone}</small>
                </td>
                <td><span class="badge ${priorityBadgeClass}">${entry.priority}</span></td>
                <td>${(index + 1) * 10} mins</td>
                <td><span class="badge bg-warning text-dark">Waiting</span></td>
                <td class="text-end pe-4">
                    <button class="btn btn-sm btn-primary call-btn" data-id="${entry.id}">Call</button>
                </td>
            `;

            row.querySelector('.call-btn').addEventListener('click', () => callPatient(entry));
            queueList.appendChild(row);
        });
    }

    function callPatient(entry) {
        currentEntry = entry;
        updateQueueStatus(entry.id, 'IN_PROGRESS');
        updateUI();
    }

    nextBtn.addEventListener('click', () => {
        const firstRow = queueList.querySelector('.call-btn');
        if (firstRow) {
            firstRow.click();
        } else {
            alert('No more patients in queue!');
        }
    });

    completeBtn.addEventListener('click', () => {
        if (currentEntry) {
            updateQueueStatus(currentEntry.id, 'COMPLETED');
            completedCount++;
            document.getElementById('completedCount').innerText = completedCount;
            currentEntry = null;
            updateUI();
            refreshQueue();
        }
    });

    function updateQueueStatus(entryId, status) {
        fetch(`/api/queue/${entryId}/status?status=${status}`, {
            method: 'PUT',
            headers: getAuthHeaders()
        })
            .then(response => {
                if (response.ok) {
                    if (status === 'IN_PROGRESS') refreshQueue();
                }
            });
    }

    function updateUI() {
        const nameEl = document.getElementById('currentPatientName');
        const detailEl = document.getElementById('currentPatientDetails');
        const tokenEl = document.getElementById('currentToken');

        if (currentEntry) {
            nameEl.innerText = `${currentEntry.patient.firstName} ${currentEntry.patient.lastName}`;
            detailEl.innerText = `Digital Token: ${currentEntry.tokenCode}`;
            tokenEl.innerText = currentEntry.queueNumber;
            completeBtn.disabled = false;
        } else {
            nameEl.innerText = 'Waiting to start...';
            detailEl.innerText = 'Select a patient from the queue';
            tokenEl.innerText = '--';
            completeBtn.disabled = true;
        }
    }

    // Auto refresh every 30 seconds
    setInterval(refreshQueue, 30000);
});
