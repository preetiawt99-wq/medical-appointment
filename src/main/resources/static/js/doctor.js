document.addEventListener('DOMContentLoaded', () => {
    if (!API.requireAuth('DOCTOR')) return;

    const user = API.getUser();
    const docNameEls = document.querySelectorAll('.doctor-name-display');
    docNameEls.forEach(el => el.textContent = user.name || 'Doctor');

    if (document.getElementById('doctorDashboard')) {
        loadDoctorDashboard();
    }
    if (document.getElementById('doctorAppointmentsTable')) {
        loadDoctorAppointments();
    }
    if (document.getElementById('availabilitySection')) {
        initDoctorAvailability();
    }
});

async function loadDoctorDashboard() {
    try {
        const [profile, appointments, slots] = await Promise.all([
            API.get('/doctors/me'),
            API.get('/doctors/me/appointments'),
            API.get('/doctors/me/slots')
        ]);

        document.getElementById('doctorSpecialization').textContent = profile.specialization;
        document.getElementById('doctorEmail').textContent = profile.email;
        document.getElementById('doctorPhone').textContent = profile.phone || 'N/A';
        document.getElementById('doctorHours').textContent = profile.availability || 'N/A';

        document.getElementById('totalDocAppointments').textContent = appointments.length;
        const pending = appointments.filter(a => a.status === 'PENDING');
        document.getElementById('pendingDocAppointments').textContent = pending.length;
        const confirmed = appointments.filter(a => a.status === 'CONFIRMED');
        document.getElementById('confirmedDocAppointments').textContent = confirmed.length;
        document.getElementById('totalSlotsCount').textContent = slots.length;

        const listDiv = document.getElementById('todayAppointmentsList');
        if (appointments.length > 0) {
            listDiv.innerHTML = '';
            appointments.slice(0, 5).forEach(app => {
                const item = document.createElement('div');
                item.className = 'list-group-item d-flex justify-content-between align-items-center py-3';
                item.innerHTML = `
                    <div>
                        <h6 class="mb-1 fw-bold">${app.patientName} (${app.patientMobile})</h6>
                        <small class="text-muted"><i class="bi bi-calendar3 me-1"></i>${app.appointmentDate} at ${app.appointmentTime} | ${app.reason || 'General'}</small>
                        ${app.patientBloodGroup ? `<span class="badge bg-danger-subtle text-danger ms-2">${app.patientBloodGroup}</span>` : ''}
                    </div>
                    <span class="badge badge-${app.status.toLowerCase()}">${app.status}</span>
                `;
                listDiv.appendChild(item);
            });
        } else {
            listDiv.innerHTML = '<p class="text-muted small p-3 mb-0">No appointments scheduled.</p>';
        }
    } catch (err) {
        console.error('Failed to load doctor dashboard:', err);
    }
}

async function loadDoctorAppointments() {
    const tbody = document.getElementById('doctorAppointmentsBody');
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="spinner-border spinner-border-sm text-primary"></span> Loading appointments...</td></tr>';

    try {
        const appointments = await API.get('/doctors/me/appointments');
        tbody.innerHTML = '';

        if (appointments.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No appointments booked with you yet.</td></tr>';
            return;
        }

        appointments.forEach(app => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-semibold">#${app.appointmentId}</td>
                <td>
                    <div class="fw-bold">${app.patientName}</div>
                    <small class="text-muted"><i class="bi bi-telephone me-1"></i>${app.patientMobile}</small>
                    <div class="mt-1">
                        ${app.patientBloodGroup ? `<span class="badge bg-danger small me-1">Blood: ${app.patientBloodGroup}</span>` : ''}
                        ${app.patientMedicalHistory ? `<span class="badge bg-secondary-subtle text-dark border small" title="${app.patientMedicalHistory}">History: ${app.patientMedicalHistory.substring(0, 30)}${app.patientMedicalHistory.length > 30 ? '...' : ''}</span>` : '<span class="text-muted small">No past history</span>'}
                    </div>
                </td>
                <td>
                    <div>${app.appointmentDate}</div>
                    <small class="text-muted">${app.appointmentTime}</small>
                </td>
                <td>
                    <div>${app.reason || '<span class="text-muted">General</span>'}</div>
                    ${app.doctorNotes ? `<div class="badge bg-success-subtle text-success border border-success-subtle mt-1 text-wrap text-start"><i class="bi bi-prescription me-1"></i><strong>Advice:</strong> ${app.doctorNotes}</div>` : ''}
                </td>
                <td><span class="badge badge-${app.status.toLowerCase()}">${app.status}</span></td>
                <td>
                    <div class="btn-group btn-group-sm">
                        ${app.status === 'PENDING' ? `
                            <button class="btn btn-outline-success status-btn" data-id="${app.appointmentId}" data-status="CONFIRMED"><i class="bi bi-check me-1"></i>Confirm</button>
                        ` : ''}
                        ${app.status === 'CONFIRMED' ? `
                            <button class="btn btn-outline-primary complete-modal-trigger" 
                                data-id="${app.appointmentId}" 
                                data-patient="${app.patientName}">
                                <i class="bi bi-clipboard2-check me-1"></i>Complete
                            </button>
                        ` : ''}
                        ${(app.status === 'PENDING' || app.status === 'CONFIRMED') ? `
                            <button class="btn btn-outline-danger status-btn" data-id="${app.appointmentId}" data-status="CANCELLED"><i class="bi bi-x me-1"></i>Cancel</button>
                        ` : '<span class="text-muted small">—</span>'}
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Direct status updates (Confirm / Cancel)
        document.querySelectorAll('.status-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                const status = btn.getAttribute('data-status');
                if (status === 'CANCELLED' && !confirm(`Are you sure you want to cancel appointment #${id}?`)) {
                    return;
                }
                try {
                    await API.put(`/appointments/${id}/status`, { status });
                    loadDoctorAppointments();
                } catch (err) {
                    alert('Failed to update status: ' + err.message);
                }
            });
        });

        // Complete Consultation Modal trigger
        const completeModalEl = document.getElementById('completeConsultationModal');
        let completeModal = null;
        if (completeModalEl) {
            completeModal = new bootstrap.Modal(completeModalEl);
        }

        document.querySelectorAll('.complete-modal-trigger').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                const patName = btn.getAttribute('data-patient');

                document.getElementById('completeModalAppId').value = id;
                document.getElementById('completeModalAppRef').textContent = `#${id}`;
                document.getElementById('completeModalPatName').textContent = patName;
                document.getElementById('doctorNotesInput').value = '';

                if (completeModal) completeModal.show();
            });
        });

        const confirmCompleteBtn = document.getElementById('confirmCompleteBtn');
        if (confirmCompleteBtn) {
            confirmCompleteBtn.onclick = async () => {
                const id = document.getElementById('completeModalAppId').value;
                const notes = document.getElementById('doctorNotesInput').value.trim();

                if (!notes) {
                    alert('Please enter consultation notes or prescription advice for the patient.');
                    return;
                }

                confirmCompleteBtn.disabled = true;
                confirmCompleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Saving...';

                try {
                    await API.put(`/appointments/${id}/status`, {
                        status: 'COMPLETED',
                        doctorNotes: notes
                    });
                    if (completeModal) completeModal.hide();
                    loadDoctorAppointments();
                } catch (err) {
                    alert('Failed to complete appointment: ' + err.message);
                } finally {
                    confirmCompleteBtn.disabled = false;
                    confirmCompleteBtn.innerHTML = '<i class="bi bi-check-circle me-1"></i>Save & Complete';
                }
            };
        }

    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Error loading appointments.</td></tr>';
    }
}

async function initDoctorAvailability() {
    const addSlotForm = document.getElementById('addSlotForm');
    const slotDateInput = document.getElementById('newSlotDate');
    const slotTimeInput = document.getElementById('newSlotTime');
    const alertBox = document.getElementById('slotAlert');
    const slotsTableBody = document.getElementById('slotsTableBody');

    slotDateInput.min = new Date().toISOString().split('T')[0];

    async function refreshSlots() {
        slotsTableBody.innerHTML = '<tr><td colspan="5" class="text-center py-3"><span class="spinner-border spinner-border-sm"></span> Loading slots...</td></tr>';
        try {
            const slots = await API.get('/doctors/me/slots');
            slotsTableBody.innerHTML = '';
            if (slots.length === 0) {
                slotsTableBody.innerHTML = '<tr><td colspan="5" class="text-center py-3 text-muted">No slots added yet. Use the form above to add your slots.</td></tr>';
                return;
            }

            slots.forEach(s => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>#${s.slotId}</td>
                    <td>${s.slotDate}</td>
                    <td>${s.slotTime}</td>
                    <td>
                        ${s.isBooked 
                            ? '<span class="badge bg-danger">Booked</span>' 
                            : '<span class="badge bg-success">Available</span>'}
                    </td>
                    <td>
                        ${s.isBooked 
                            ? '<span class="badge bg-secondary-subtle text-secondary small">Locked (Booked)</span>' 
                            : `<button class="btn btn-outline-danger btn-sm delete-slot-btn" data-id="${s.slotId}"><i class="bi bi-trash me-1"></i>Delete</button>`}
                    </td>
                `;
                slotsTableBody.appendChild(tr);
            });

            document.querySelectorAll('.delete-slot-btn').forEach(btn => {
                btn.addEventListener('click', async () => {
                    const slotId = btn.getAttribute('data-id');
                    if (!confirm(`Are you sure you want to delete slot #${slotId}?`)) return;
                    try {
                        await API.delete(`/doctors/me/slots/${slotId}`);
                        refreshSlots();
                    } catch (err) {
                        alert('Failed to delete slot: ' + (err.message || 'Error occurred'));
                    }
                });
            });
        } catch (err) {
            slotsTableBody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-3">Failed to load slots.</td></tr>';
        }
    }

    addSlotForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        alertBox.classList.add('d-none');
        const submitBtn = document.getElementById('addSlotBtn');
        submitBtn.disabled = true;

        try {
            let timeVal = slotTimeInput.value;
            if (timeVal.split(':').length === 2) {
                timeVal += ':00';
            }

            await API.post('/doctors/me/slots', {
                slotDate: slotDateInput.value,
                slotTime: timeVal
            });

            alertBox.className = 'alert alert-success';
            alertBox.textContent = 'Time slot successfully created!';
            alertBox.classList.remove('d-none');
            refreshSlots();
        } catch (err) {
            alertBox.className = 'alert alert-danger';
            alertBox.textContent = err.message || 'Error adding slot.';
            alertBox.classList.remove('d-none');
        } finally {
            submitBtn.disabled = false;
        }
    });

    refreshSlots();
}