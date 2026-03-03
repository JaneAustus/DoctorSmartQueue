document.addEventListener('DOMContentLoaded', function () {
    // Get logged in user from localStorage
    const savedUser = JSON.parse(localStorage.getItem('hospitalUser'));

    if (!savedUser) {
        alert('Please login first');
        window.location.href = 'index.html';
        return;
    }

    const patientId = savedUser.id;
    document.getElementById('userName').innerText = savedUser.firstName;
    const headerPic = document.getElementById('headerProfilePic');
    if (headerPic) {
        headerPic.src = `https://ui-avatars.com/api/?name=${savedUser.firstName}+${savedUser.lastName}&background=0d6efd&color=fff`;
    }

    let liveTrackingInterval = null;
    let allDoctors = []; // Cache all doctors

    loadDoctors();

    function loadDoctors() {
        const doctorsList = document.getElementById('doctorsList');
        console.log('Fetching doctors from /api/hospital/doctors...');

        // Fetch doctors from our API
        fetch('/api/hospital/doctors')
            .then(response => {
                if (!response.ok) throw new Error('Server returned ' + response.status);
                return response.json();
            })
            .then(doctors => {
                allDoctors = doctors;
                renderDoctors(doctors);
            })
            .catch(error => {
                console.error('Error fetching doctors:', error);
                doctorsList.innerHTML = `<div class="col-12 text-center text-danger">Failed to load doctors: ${error.message}</div>`;
            });
    }

    function renderDoctors(doctors) {
        const doctorsList = document.getElementById('doctorsList');
        doctorsList.innerHTML = '';

        if (doctors.length === 0) {
            doctorsList.innerHTML = '<div class="col-12 text-center text-muted py-5">No doctors found matching your search.</div>';
            return;
        }

        doctors.forEach(doctor => {
            const card = createDoctorCard(doctor);
            doctorsList.appendChild(card);
        });
    }

    // Search and Filter Listeners
    const doctorSearch = document.getElementById('doctorSearch');
    const deptFilter = document.getElementById('deptFilter');

    if (doctorSearch) {
        doctorSearch.addEventListener('input', applyFilters);
    }
    if (deptFilter) {
        deptFilter.addEventListener('change', applyFilters);
    }

    function applyFilters() {
        const searchTerm = doctorSearch.value.toLowerCase();
        const selectedDept = deptFilter.value;

        const filtered = allDoctors.filter(doc => {
            const matchesSearch = doc.name.toLowerCase().includes(searchTerm) ||
                doc.specialization.toLowerCase().includes(searchTerm);

            const matchesDept = selectedDept === 'ALL' || doc.department.name === selectedDept;

            return matchesSearch && matchesDept;
        });

        renderDoctors(filtered);
    }

    function createDoctorCard(doctor) {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-xl-4';

        const availabilityClass = doctor.available ? 'bg-success' : 'bg-danger';
        const availabilityText = doctor.available ? 'Available' : 'Unavailable';

        col.innerHTML = `
            <div class="card doctor-card shadow-sm h-100">
                <div class="card-body p-4">
                    <div class="d-flex align-items-center mb-3">
                        <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(doctor.name)}&background=e7f1ff&color=0d6efd" class="rounded-pill me-3" width="60" alt="Doctor">
                        <div>
                            <h5 class="fw-bold mb-0">${doctor.name}</h5>
                            <small class="text-primary fw-medium">${doctor.specialization}</small>
                        </div>
                    </div>
                    <div class="mb-3">
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted small"><i class="fas fa-hospital-alt me-1"></i> Department:</span>
                            <span class="small fw-bold">${doctor.department.name}</span>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted small"><i class="fas fa-door-open me-1"></i> Room:</span>
                            <span class="small fw-bold">${doctor.roomNumber}</span>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span class="text-muted small"><i class="fas fa-circle me-1" style="font-size: 8px; vertical-align: middle;"></i> Status:</span>
                            <span class="badge ${availabilityClass} status-badge">${availabilityText}</span>
                        </div>
                    </div>
                    <button class="btn btn-outline-primary w-100 fw-bold join-queue-btn" 
                            data-doctor-id="${doctor.id}" 
                            data-doctor-name="${doctor.name}"
                            ${!doctor.available ? 'disabled' : ''}>
                        <i class="fas fa-user-plus me-1"></i> Join Queue
                    </button>
                </div>
            </div>
        `;

        // Add event listener to the button
        const btn = col.querySelector('.join-queue-btn');
        btn.addEventListener('click', () => joinQueue(doctor.id, doctor.name, btn));

        return col;
    }

    let selectedDoctorId = null;
    let selectedDoctorName = null;
    let selectedButton = null;

    function joinQueue(doctorId, doctorName, button) {
        selectedDoctorId = doctorId;
        selectedDoctorName = doctorName;
        selectedButton = button;

        // Show priority modal instead of joining directly
        const modal = new bootstrap.Modal(document.getElementById('priorityModal'));
        modal.show();
    }

    // Handle priority selection
    document.querySelectorAll('.priority-option').forEach(btn => {
        btn.addEventListener('click', function () {
            const priority = this.getAttribute('data-priority');
            const modalElement = document.getElementById('priorityModal');
            const modal = bootstrap.Modal.getInstance(modalElement);
            modal.hide();

            submitQueueJoin(priority);
        });
    });

    function submitQueueJoin(priority) {
        if (!selectedDoctorId) return;

        const originalText = selectedButton.innerHTML;
        selectedButton.disabled = true;
        selectedButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Joining...';

        fetch(`/api/queue/join?patientId=${patientId}&doctorId=${selectedDoctorId}&priority=${priority}`, {
            method: 'POST'
        })
            .then(async response => {
                if (response.ok) {
                    const entry = await response.json();
                    showQueueSuccess(entry, selectedDoctorName);
                } else {
                    const errorMsg = await response.text();
                    throw new Error(errorMsg);
                }
            })
            .catch(error => {
                alert('Error joining queue: ' + error.message);
                selectedButton.disabled = false;
                selectedButton.innerHTML = originalText;
            });
    }

    function showQueueSuccess(entry, doctorName) {
        const container = document.getElementById('currentQueueContainer');
        const tokenNumberSpan = document.getElementById('myQueueNumber');
        const digitalTokenSpan = document.getElementById('myDigitalToken');
        const doctorNameSpan = document.getElementById('currentDoctorName');
        const doctorRoomSpan = document.getElementById('currentDoctorRoom');
        const doctorDeptSpan = document.getElementById('currentDoctorDept');
        const waitTimeSpan = document.getElementById('estWaitTime');

        // Populate the enhanced Token Card
        tokenNumberSpan.innerText = entry.queueNumber;
        digitalTokenSpan.innerText = entry.tokenCode;
        doctorNameSpan.innerText = doctorName;
        doctorRoomSpan.innerText = `Room ${entry.doctor.roomNumber}`;
        doctorDeptSpan.innerText = entry.doctor.department.name;
        waitTimeSpan.innerText = entry.estimatedWaitTime > 0 ? `${entry.estimatedWaitTime} mins` : "Next In Line";

        // Show the container with animation
        container.style.display = 'block';
        container.classList.add('animate__animated', 'animate__fadeInDown');
        container.scrollIntoView({ behavior: 'smooth' });

        // Start live tracking
        startLiveTracking(entry.id);
    }

    function startLiveTracking(entryId) {
        if (liveTrackingInterval) clearInterval(liveTrackingInterval);

        // Auto refresh every 10 seconds
        liveTrackingInterval = setInterval(() => refreshTokenStatus(entryId), 10000);
    }

    function refreshTokenStatus(entryId) {
        fetch(`/api/queue/${entryId}`)
            .then(response => response.json())
            .then(entry => {
                const waitTimeSpan = document.getElementById('estWaitTime');
                const statusBadge = document.querySelector('.status-badge');

                // Update wait time
                waitTimeSpan.innerText = entry.estimatedWaitTime > 0 ? `${entry.estimatedWaitTime} mins` : "Next In Line";

                // Update status badge
                if (entry.status === 'IN_PROGRESS') {
                    statusBadge.className = 'badge bg-primary status-badge p-2 px-3';
                    statusBadge.innerHTML = '<i class="fas fa-user-md me-1"></i> Consulting Now';
                    waitTimeSpan.innerText = "It's your turn!";
                } else if (entry.status === 'COMPLETED') {
                    statusBadge.className = 'badge bg-success status-badge p-2 px-3';
                    statusBadge.innerHTML = '<i class="fas fa-check me-1"></i> Completed';
                    clearInterval(liveTrackingInterval);
                    alert("Your consultation is complete. Thank you!");
                } else if (entry.status === 'CANCELLED') {
                    statusBadge.className = 'badge bg-secondary status-badge p-2 px-3';
                    statusBadge.innerHTML = '<i class="fas fa-times me-1"></i> Cancelled';
                    clearInterval(liveTrackingInterval);
                }
            })
            .catch(err => console.error('Tracking Error:', err));
    }

    // Live Hospital Monitor Logic
    const trackHospitalBtn = document.getElementById('trackHospitalBtn');
    let monitorInterval = null;

    if (trackHospitalBtn) {
        trackHospitalBtn.addEventListener('click', function () {
            const modal = new bootstrap.Modal(document.getElementById('liveMonitorModal'));
            modal.show();
            refreshLiveOverview();

            if (monitorInterval) clearInterval(monitorInterval);
            monitorInterval = setInterval(refreshLiveOverview, 10000);
        });

        // Check for URL parameter to auto-open modal
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('track') === 'true') {
            trackHospitalBtn.click();
        }
    }

    const monitorModalElement = document.getElementById('liveMonitorModal');
    if (monitorModalElement) {
        monitorModalElement.addEventListener('hidden.bs.modal', function () {
            if (monitorInterval) {
                clearInterval(monitorInterval);
                monitorInterval = null;
            }
        });
    }

    function refreshLiveOverview() {
        fetch('/api/hospital/live-overview')
            .then(res => res.json())
            .then(data => {
                const list = document.getElementById('liveOverviewList');
                if (!list) return;
                list.innerHTML = '';

                data.forEach(item => {
                    const serving = item.servingToken || '--';
                    const col = document.createElement('div');
                    col.className = 'col-md-4';
                    col.innerHTML = `
                        <div class="card border-0 shadow-sm text-center p-3 h-100" style="border-radius: 20px; border-top: 5px solid #0d6efd !important;">
                            <div class="bg-primary bg-opacity-10 p-2 rounded-circle mx-auto mb-2" style="width: fit-content;">
                                <i class="fas fa-stethoscope text-primary"></i>
                            </div>
                            <h6 class="fw-bold mb-0">${item.doctorName}</h6>
                            <small class="text-muted mb-3 d-block">${item.specialization}</small>
                            <div class="p-2 bg-light rounded-4 mb-2">
                                <small class="d-block text-muted" style="font-size: 0.7rem;">SERVING NOW</small>
                                <h4 class="fw-bold text-primary mb-0">#${serving}</h4>
                            </div>
                            <div class="small fw-medium text-warning">
                                <i class="fas fa-users me-1"></i> ${item.waitingCount} Waiting
                            </div>
                        </div>
                    `;
                    list.appendChild(col);
                });
            })
            .catch(err => console.error('Monitor Error:', err));
    }
});
