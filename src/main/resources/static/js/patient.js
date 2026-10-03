document.addEventListener('DOMContentLoaded', () => {
    if (!API.requireAuth('PATIENT')) return;

    const user = API.getUser();
    const patientNameEls = document.querySelectorAll('.patient-name-display');
    patientNameEls.forEach(el => el.textContent = user.name || 'Patient');

    if (document.getElementById('patientDashboard')) {
        loadDashboardData();
    }
    if (document.getElementById('bookingSection')) {
        initBookingPage();
    }
    if (document.getElementById('myAppointmentsTable')) {
        loadMyAppointments();
    }
    if (document.getElementById('patientProfileSection')) {
        initProfilePage();
    }
});

async function loadDashboardData() {
    try {
        const [profile, appointments, risks] = await Promise.all([
            API.get('/patients/me'),
            API.get('/patients/me/appointments'),
            API.get('/patients/me/risk-reports')
        ]);

        document.getElementById('patientName').textContent = profile.name;
        document.getElementById('patientEmail').textContent = profile.email;
        document.getElementById('patientMobile').textContent = profile.mobile;
        document.getElementById('patientAge').textContent = `${profile.age} yrs (${profile.gender})`;
        
        const bloodEl = document.getElementById('patientBloodGroup');
        if (bloodEl) {
            bloodEl.textContent = profile.bloodGroup || 'Not specified';
        }
        const medEl = document.getElementById('patientMedicalHistory');
        if (medEl) {
            medEl.textContent = profile.medicalHistory || 'None recorded';
        }

        document.getElementById('totalAppointmentsCount').textContent = appointments.length;
        const upcoming = appointments.filter(a => a.status === 'PENDING' || a.status === 'CONFIRMED');
        document.getElementById('upcomingAppointmentsCount').textContent = upcoming.length;
        document.getElementById('riskChecksCount').textContent = risks.length;

        const recentAppDiv = document.getElementById('latestAppointmentWidget');
        if (appointments.length > 0) {
            const latest = appointments[0];
            recentAppDiv.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-1 fw-bold">${latest.doctorName} (${latest.doctorSpecialization})</h6>
                        <small class="text-muted"><i class="bi bi-clock me-1"></i>${latest.appointmentDate} at ${latest.appointmentTime}</small>
                        ${latest.doctorNotes ? `<div class="mt-1 small text-success"><i class="bi bi-prescription me-1"></i><strong>Advice:</strong> ${latest.doctorNotes}</div>` : ''}
                    </div>
                    <span class="badge badge-${latest.status.toLowerCase()}">${latest.status}</span>
                </div>
            `;
        } else {
            recentAppDiv.innerHTML = `<p class="text-muted mb-0 small">No appointments booked yet.</p>`;
        }

        const recentRiskDiv = document.getElementById('latestRiskWidget');
        if (risks.length > 0) {
            const latest = risks[0];
            const badgeClass = `risk-badge-${latest.riskLevel.toLowerCase()}`;
            recentRiskDiv.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="${badgeClass} me-2">${latest.riskLevel} RISK</span>
                        <small class="text-muted">${latest.reportDate}</small>
                        <p class="small text-secondary mt-1 mb-0 text-truncate" style="max-width: 280px;">${latest.result}</p>
                    </div>
                </div>
            `;
        } else {
            recentRiskDiv.innerHTML = `<p class="text-muted mb-0 small">No risk assessments performed.</p>`;
        }
    } catch (err) {
        console.error('Error loading patient dashboard:', err);
    }
}

async function initProfilePage() {
    const form = document.getElementById('profileForm');
    const alertBox = document.getElementById('profileAlert');
    const btn = document.getElementById('saveProfileBtn');

    try {
        const p = await API.get('/patients/me');
        document.getElementById('profName').value = p.name || '';
        document.getElementById('profEmail').value = p.email || '';
        document.getElementById('profMobile').value = p.mobile || '';
        document.getElementById('profAge').value = p.age || '';
        document.getElementById('profGender').value = p.gender || 'Male';
        document.getElementById('profAddress').value = p.address || '';
        document.getElementById('profBloodGroup').value = p.bloodGroup || '';
        document.getElementById('profMedicalHistory').value = p.medicalHistory || '';
    } catch (err) {
        alertBox.className = 'alert alert-danger';
        alertBox.textContent = 'Failed to load profile details.';
        alertBox.classList.remove('d-none');
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        alertBox.classList.add('d-none');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Saving...';

        const payload = {
            name: document.getElementById('profName').value.trim(),
            mobile: document.getElementById('profMobile').value.trim(),
            age: parseInt(document.getElementById('profAge').value, 10),
            gender: document.getElementById('profGender').value,
            address: document.getElementById('profAddress').value.trim(),
            bloodGroup: document.getElementById('profBloodGroup').value,
            medicalHistory: document.getElementById('profMedicalHistory').value.trim()
        };

        try {
            const updated = await API.put('/patients/me', payload);
            alertBox.className = 'alert alert-success';
            alertBox.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i>Profile and medical records successfully updated!';
            alertBox.classList.remove('d-none');

            // Update cached username
            const currentUser = API.getUser() || {};
            currentUser.name = updated.name;
            localStorage.setItem('user', JSON.stringify(currentUser));
            document.querySelectorAll('.patient-name-display').forEach(el => el.textContent = updated.name);
        } catch (err) {
            alertBox.className = 'alert alert-danger';
            alertBox.textContent = err.message || 'Failed to update profile.';
            alertBox.classList.remove('d-none');
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-check2-circle me-1"></i>Save Profile Changes';
        }
    });
}

async function initBookingPage() {
    const doctorSelect = document.getElementById('doctorSelect');
    const slotDateInput = document.getElementById('slotDate');
    const slotsContainer = document.getElementById('slotsContainer');
    const reasonInput = document.getElementById('bookingReason');
    const bookBtn = document.getElementById('confirmBookingBtn');
    const alertBox = document.getElementById('bookingAlert');

    const todayStr = new Date().toISOString().split('T')[0];
    slotDateInput.min = todayStr;
    slotDateInput.value = todayStr;

    let selectedSlotId = null;

    // Check URL parameters for pre-filling from Risk Check
    const urlParams = new URLSearchParams(window.location.search);
    const prefillReason = urlParams.get('reason');
    const fromRisk = urlParams.get('fromRisk');
    const prefillDoctorId = urlParams.get('doctorId');

    if (prefillReason && reasonInput) {
        reasonInput.value = prefillReason;
    }

    if (fromRisk && alertBox) {
        alertBox.className = 'alert alert-info py-2 d-flex align-items-center';
        alertBox.innerHTML = '<i class="bi bi-info-circle-fill me-2 fs-5"></i><div><strong>Health Risk Follow-up:</strong> Consultation reason pre-filled from your recent risk assessment. Please choose an available doctor and slot below.</div>';
        alertBox.classList.remove('d-none');
    }

    try {
        const doctors = await API.get('/doctors');
        doctorSelect.innerHTML = '<option value="" disabled selected>Choose a Doctor</option>';
        doctors.forEach(doc => {
            const opt = document.createElement('option');
            opt.value = doc.doctorId;
            opt.textContent = `${doc.name} — ${doc.specialization}`;
            doctorSelect.appendChild(opt);
        });

        if (prefillDoctorId) {
            doctorSelect.value = prefillDoctorId;
            loadAvailableSlots();
        }
    } catch (err) {
        alertBox.className = 'alert alert-danger';
        alertBox.textContent = 'Failed to load doctors list.';
        alertBox.classList.remove('d-none');
    }

    async function loadAvailableSlots() {
        const doctorId = doctorSelect.value;
        const date = slotDateInput.value;
        selectedSlotId = null;
        bookBtn.disabled = true;
        slotsContainer.innerHTML = '<div class="text-center py-3"><span class="spinner-border spinner-border-sm text-primary"></span> Loading slots...</div>';

        if (!doctorId || !date) {
            slotsContainer.innerHTML = '<p class="text-muted small">Select a doctor and date to view available time slots.</p>';
            return;
        }

        try {
            const slots = await API.get(`/doctors/${doctorId}/slots?date=${date}`);
            slotsContainer.innerHTML = '';

            if (slots.length === 0) {
                slotsContainer.innerHTML = '<div class="alert alert-info py-2 small mb-0">No available slots on this date. Please choose another date or doctor.</div>';
                return;
            }

            const row = document.createElement('div');
            row.className = 'row g-2';

            slots.forEach(slot => {
                const col = document.createElement('div');
                col.className = 'col-6 col-md-4 col-lg-3';
                col.innerHTML = `
                    <button type="button" class="btn btn-outline-primary w-100 py-2 slot-btn" data-slot-id="${slot.slotId}">
                        <i class="bi bi-clock me-1"></i>${slot.slotTime.substring(0, 5)}
                    </button>
                `;
                row.appendChild(col);
            });

            slotsContainer.appendChild(row);

            document.querySelectorAll('.slot-btn').forEach(btn => {
                btn.addEventListener('click', () => {
                    document.querySelectorAll('.slot-btn').forEach(b => {
                        b.classList.remove('btn-primary', 'active');
                        b.classList.add('btn-outline-primary');
                    });
                    btn.classList.remove('btn-outline-primary');
                    btn.classList.add('btn-primary', 'active');
                    selectedSlotId = btn.getAttribute('data-slot-id');
                    bookBtn.disabled = false;
                });
            });
        } catch (err) {
            slotsContainer.innerHTML = '<div class="alert alert-danger py-2 small">Error loading slots.</div>';
        }
    }

    doctorSelect.addEventListener('change', loadAvailableSlots);
    slotDateInput.addEventListener('change', loadAvailableSlots);

    bookBtn.addEventListener('click', async () => {
        if (!selectedSlotId) return;

        alertBox.classList.add('d-none');
        bookBtn.disabled = true;
        bookBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Booking...';

        try {
            await API.post('/appointments', {
                doctorId: doctorSelect.value,
                slotId: selectedSlotId,
                reason: reasonInput.value.trim() || 'General Consultation'
            });

            alertBox.className = 'alert alert-success';
            alertBox.innerHTML = '<strong>Success!</strong> Appointment booked successfully. Redirecting to your appointments...';
            alertBox.classList.remove('d-none');

            setTimeout(() => {
                window.location.href = '/patient/my-appointments.html';
            }, 1500);
        } catch (err) {
            alertBox.className = 'alert alert-danger';
            alertBox.textContent = err.message || 'Failed to book appointment.';
            alertBox.classList.remove('d-none');
            bookBtn.disabled = false;
            bookBtn.textContent = 'Confirm Booking';
        }
    });
}

async function loadMyAppointments() {
    const tbody = document.getElementById('myAppointmentsBody');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="spinner-border spinner-border-sm text-primary"></span> Loading appointments...</td></tr>';

    try {
        const appointments = await API.get('/patients/me/appointments');
        tbody.innerHTML = '';

        if (appointments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No appointments found. <a href="/patient/book-appointment.html">Book your first appointment</a></td></tr>';
            return;
        }

        appointments.forEach(app => {
            const tr = document.createElement('tr');
            const canModify = app.status === 'PENDING' || app.status === 'CONFIRMED';
            tr.innerHTML = `
                <td class="fw-semibold">#${app.appointmentId}</td>
                <td>
                    <div class="fw-bold">${app.doctorName}</div>
                    <small class="text-muted">${app.doctorSpecialization}</small>
                </td>
                <td>
                    <div>${app.appointmentDate}</div>
                    <small class="text-muted">${app.appointmentTime}</small>
                </td>
                <td>
                    <div>${app.reason || '<span class="text-muted">General</span>'}</div>
                    ${app.doctorNotes ? `<div class="badge bg-success-subtle text-success border border-success-subtle mt-1 text-wrap text-start"><i class="bi bi-prescription me-1"></i>${app.doctorNotes}</div>` : ''}
                </td>
                <td><span class="badge badge-${app.status.toLowerCase()}">${app.status}</span></td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-info view-slip-btn" data-app='${JSON.stringify(app).replace(/'/g, "&apos;")}'>
                            <i class="bi bi-file-earmark-medical me-1"></i>Slip
                        </button>
                        ${canModify ? `
                            <button class="btn btn-outline-warning reschedule-app-btn" 
                                data-id="${app.appointmentId}" 
                                data-doctor-id="${app.doctorId}" 
                                data-doctor-name="${app.doctorName}">
                                <i class="bi bi-arrow-repeat me-1"></i>Reschedule
                            </button>
                            <button class="btn btn-outline-danger cancel-app-btn" data-id="${app.appointmentId}">
                                <i class="bi bi-x-circle me-1"></i>Cancel
                            </button>
                        ` : ''}
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Cancel handler
        document.querySelectorAll('.cancel-app-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (confirm(`Are you sure you want to cancel appointment #${id}?`)) {
                    btn.disabled = true;
                    btn.textContent = 'Cancelling...';
                    try {
                        await API.put(`/appointments/${id}/cancel`);
                        loadMyAppointments();
                    } catch (err) {
                        alert('Error cancelling: ' + err.message);
                        btn.disabled = false;
                        btn.textContent = 'Cancel';
                    }
                }
            });
        });

        // Reschedule handler
        initRescheduleModalHandlers();

        // Slip modal handler
        document.querySelectorAll('.view-slip-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const app = JSON.parse(btn.getAttribute('data-app'));
                populateAppointmentSlip(app);
            });
        });

    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Failed to load appointments.</td></tr>';
    }
}

function populateAppointmentSlip(app) {
    document.getElementById('slipAppId').textContent = `#${app.appointmentId}`;
    document.getElementById('slipBookedAt').textContent = `Booked on: ${app.createdAt ? app.createdAt.replace('T', ' ').substring(0, 16) : app.appointmentDate}`;
    
    const badge = document.getElementById('slipStatusBadge');
    badge.className = `badge badge-${app.status.toLowerCase()} fs-6`;
    badge.textContent = app.status;

    document.getElementById('slipPatientName').textContent = app.patientName || '-';
    document.getElementById('slipPatientMobile').textContent = app.patientMobile || '-';
    document.getElementById('slipPatientBloodGroup').textContent = app.patientBloodGroup || 'Not specified';
    document.getElementById('slipPatientMedicalHistory').textContent = app.patientMedicalHistory || 'None recorded';

    document.getElementById('slipDoctorName').textContent = app.doctorName || '-';
    document.getElementById('slipDoctorSpec').textContent = app.doctorSpecialization || '-';
    document.getElementById('slipAppDate').textContent = app.appointmentDate || '-';
    document.getElementById('slipAppTime').textContent = app.appointmentTime || '-';
    document.getElementById('slipReason').textContent = app.reason || 'General Consultation';

    const notesSec = document.getElementById('slipDoctorNotesSection');
    const notesEl = document.getElementById('slipDoctorNotes');
    if (app.doctorNotes) {
        notesEl.textContent = app.doctorNotes;
        notesSec.classList.remove('d-none');
    } else {
        notesSec.classList.add('d-none');
    }

    document.getElementById('slipTokenHash').textContent = `MED-AUTH-${app.appointmentId}-${app.patientId || '101'}-SECURE`;

    const modalEl = document.getElementById('appointmentSlipModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
}

function initRescheduleModalHandlers() {
    const modalEl = document.getElementById('rescheduleModal');
    if (!modalEl) return;
    const modal = new bootstrap.Modal(modalEl);
    const dateInput = document.getElementById('rescheduleDate');
    const slotsDiv = document.getElementById('rescheduleSlotsContainer');
    const confirmBtn = document.getElementById('confirmRescheduleBtn');
    const alertBox = document.getElementById('rescheduleAlert');
    const reasonInput = document.getElementById('rescheduleReason');

    let currentAppId = null;
    let currentDoctorId = null;
    let selectedNewSlotId = null;

    const todayStr = new Date().toISOString().split('T')[0];
    dateInput.min = todayStr;

    document.querySelectorAll('.reschedule-app-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            currentAppId = btn.getAttribute('data-id');
            currentDoctorId = btn.getAttribute('data-doctor-id');
            const docName = btn.getAttribute('data-doctor-name');

            document.getElementById('rescheduleAppId').value = currentAppId;
            document.getElementById('rescheduleDocId').value = currentDoctorId;
            document.getElementById('rescheduleDocInfo').textContent = `Rescheduling consultation with ${docName}. Choose a new date and available slot.`;
            dateInput.value = todayStr;
            selectedNewSlotId = null;
            confirmBtn.disabled = true;
            alertBox.classList.add('d-none');

            modal.show();
            loadSlotsForReschedule();
        });
    });

    async function loadSlotsForReschedule() {
        if (!currentDoctorId || !dateInput.value) return;
        selectedNewSlotId = null;
        confirmBtn.disabled = true;
        slotsDiv.innerHTML = '<div class="text-center py-2"><span class="spinner-border spinner-border-sm text-primary"></span> Loading available slots...</div>';

        try {
            const slots = await API.get(`/doctors/${currentDoctorId}/slots?date=${dateInput.value}`);
            slotsDiv.innerHTML = '';

            if (slots.length === 0) {
                slotsDiv.innerHTML = '<div class="alert alert-info py-2 small mb-0">No available slots on this date. Please choose another date.</div>';
                return;
            }

            const row = document.createElement('div');
            row.className = 'row g-2';

            slots.forEach(slot => {
                const col = document.createElement('div');
                col.className = 'col-4';
                col.innerHTML = `
                    <button type="button" class="btn btn-outline-primary btn-sm w-100 py-2 r-slot-btn" data-slot-id="${slot.slotId}">
                        <i class="bi bi-clock me-1"></i>${slot.slotTime.substring(0, 5)}
                    </button>
                `;
                row.appendChild(col);
            });

            slotsDiv.appendChild(row);

            document.querySelectorAll('.r-slot-btn').forEach(btn => {
                btn.addEventListener('click', () => {
                    document.querySelectorAll('.r-slot-btn').forEach(b => {
                        b.classList.remove('btn-primary', 'active');
                        b.classList.add('btn-outline-primary');
                    });
                    btn.classList.remove('btn-outline-primary');
                    btn.classList.add('btn-primary', 'active');
                    selectedNewSlotId = btn.getAttribute('data-slot-id');
                    confirmBtn.disabled = false;
                });
            });
        } catch (err) {
            slotsDiv.innerHTML = '<div class="alert alert-danger py-2 small">Error loading doctor slots.</div>';
        }
    }

    dateInput.addEventListener('change', loadSlotsForReschedule);

    confirmBtn.addEventListener('click', async () => {
        if (!currentAppId || !selectedNewSlotId) return;

        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Rescheduling...';
        alertBox.classList.add('d-none');

        try {
            await API.put(`/appointments/${currentAppId}/reschedule`, {
                newSlotId: parseInt(selectedNewSlotId, 10),
                reason: reasonInput.value.trim() || 'Rescheduled by patient'
            });

            alertBox.className = 'alert alert-success';
            alertBox.textContent = 'Appointment rescheduled successfully! Refreshing...';
            alertBox.classList.remove('d-none');

            setTimeout(() => {
                modal.hide();
                loadMyAppointments();
            }, 1200);
        } catch (err) {
            alertBox.className = 'alert alert-danger';
            alertBox.textContent = err.message || 'Failed to reschedule appointment.';
            alertBox.classList.remove('d-none');
            confirmBtn.disabled = false;
            confirmBtn.textContent = 'Confirm Reschedule';
        }
    });
}