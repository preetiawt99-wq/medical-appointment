document.addEventListener('DOMContentLoaded', () => {
    if (!API.requireAuth('ADMIN')) return;

    if (document.getElementById('adminDashboard')) {
        loadAdminDashboard();
    }
    if (document.getElementById('adminDoctorsTable')) {
        loadAdminDoctors();
    }
    if (document.getElementById('adminPatientsTable')) {
        loadAdminPatients();
    }
    if (document.getElementById('adminAppointmentsTable')) {
        loadAdminAppointments();
    }
    if (document.getElementById('adminReportsSection')) {
        loadAdminReports();
    }
});

async function loadAdminDashboard() {
    try {
        const [summary, appointments] = await Promise.all([
            API.get('/admin/reports/summary'),
            API.get('/admin/appointments?size=5')
        ]);

        document.getElementById('adminTotalPatients').textContent = summary.totalPatients;
        document.getElementById('adminTotalDoctors').textContent = summary.totalDoctors;
        document.getElementById('adminTotalAppointments').textContent = summary.totalAppointments;
        document.getElementById('adminCompletedCount').textContent = summary.completedAppointments;
        document.getElementById('adminPendingCount').textContent = summary.pendingAppointments;
        document.getElementById('adminCancelledCount').textContent = summary.cancelledAppointments;
        document.getElementById('adminRiskCount').textContent = summary.totalRiskReports;

        const tbody = document.getElementById('adminRecentAppointmentsBody');
        tbody.innerHTML = '';
        if (appointments.content && appointments.content.length > 0) {
            appointments.content.forEach(a => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>#${a.appointmentId}</td>
                    <td>${a.patientName}</td>
                    <td>${a.doctorName}</td>
                    <td>${a.appointmentDate} ${a.appointmentTime}</td>
                    <td><span class="badge badge-${a.status.toLowerCase()}">${a.status}</span></td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No appointments found.</td></tr>';
        }
    } catch (err) {
        console.error('Error loading admin dashboard:', err);
    }
}

async function loadAdminDoctors() {
    const tbody = document.getElementById('adminDoctorsBody');
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><span class="spinner-border spinner-border-sm"></span> Loading doctors...</td></tr>';

    try {
        const doctors = await API.get('/admin/doctors');
        tbody.innerHTML = '';
        if (doctors.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No doctors found.</td></tr>';
            return;
        }

        doctors.forEach(d => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${d.doctorId}</td>
                <td class="fw-bold">${d.name}</td>
                <td><span class="badge bg-info text-dark">${d.specialization}</span></td>
                <td>${d.email}</td>
                <td>${d.phone || 'N/A'}</td>
                <td><small>${d.availability || 'Standard'}</small></td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary edit-doc-btn" 
                            data-id="${d.doctorId}"
                            data-name="${d.name}"
                            data-spec="${d.specialization}"
                            data-email="${d.email}"
                            data-phone="${d.phone || ''}"
                            data-avail="${d.availability || ''}">
                            <i class="bi bi-pencil me-1"></i>Edit
                        </button>
                        <button class="btn btn-outline-danger delete-doc-btn" data-id="${d.doctorId}">
                            <i class="bi bi-trash me-1"></i>Delete
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Delete handler
        document.querySelectorAll('.delete-doc-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (confirm(`Are you sure you want to delete Doctor #${id}?`)) {
                    try {
                        await API.delete(`/admin/doctors/${id}`);
                        loadAdminDoctors();
                    } catch (err) {
                        alert('Failed to delete doctor: ' + err.message);
                    }
                }
            });
        });

        // Edit Modal Trigger
        const editModalEl = document.getElementById('editDoctorModal');
        let editModal = null;
        if (editModalEl) {
            editModal = new bootstrap.Modal(editModalEl);
        }

        document.querySelectorAll('.edit-doc-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.getElementById('editDocId').value = btn.getAttribute('data-id');
                document.getElementById('editDocName').value = btn.getAttribute('data-name');
                document.getElementById('editDocSpec').value = btn.getAttribute('data-spec');
                document.getElementById('editDocEmail').value = btn.getAttribute('data-email');
                document.getElementById('editDocPhone').value = btn.getAttribute('data-phone');
                document.getElementById('editDocAvail').value = btn.getAttribute('data-avail');
                document.getElementById('editDocPassword').value = '';
                document.getElementById('editDoctorAlert').classList.add('d-none');
                if (editModal) editModal.show();
            });
        });

    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-danger py-4">Failed to load doctors.</td></tr>';
    }

    // Add Doctor Form Submit
    const addDocForm = document.getElementById('addDoctorForm');
    if (addDocForm) {
        addDocForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('addDoctorAlert');
            alertBox.classList.add('d-none');

            const payload = {
                name: document.getElementById('docName').value.trim(),
                specialization: document.getElementById('docSpec').value.trim(),
                email: document.getElementById('docEmail').value.trim(),
                phone: document.getElementById('docPhone').value.trim(),
                password: document.getElementById('docPassword').value,
                availability: document.getElementById('docAvail').value.trim()
            };

            try {
                await API.post('/admin/doctors', payload);
                const modalEl = document.getElementById('addDoctorModal');
                const modal = bootstrap.Modal.getInstance(modalEl);
                if (modal) modal.hide();
                addDocForm.reset();
                loadAdminDoctors();
            } catch (err) {
                alertBox.className = 'alert alert-danger';
                alertBox.textContent = err.message || 'Error creating doctor.';
                alertBox.classList.remove('d-none');
            }
        });
    }

    // Edit Doctor Form Submit
    const editDocForm = document.getElementById('editDoctorForm');
    if (editDocForm) {
        editDocForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById('editDoctorAlert');
            alertBox.classList.add('d-none');
            const docId = document.getElementById('editDocId').value;

            const payload = {
                name: document.getElementById('editDocName').value.trim(),
                specialization: document.getElementById('editDocSpec').value.trim(),
                phone: document.getElementById('editDocPhone').value.trim(),
                availability: document.getElementById('editDocAvail').value.trim()
            };

            const newPwd = document.getElementById('editDocPassword').value;
            if (newPwd && newPwd.trim().length > 0) {
                payload.password = newPwd.trim();
            }

            try {
                await API.put(`/admin/doctors/${docId}`, payload);
                const modalEl = document.getElementById('editDoctorModal');
                const modal = bootstrap.Modal.getInstance(modalEl);
                if (modal) modal.hide();
                loadAdminDoctors();
            } catch (err) {
                alertBox.className = 'alert alert-danger';
                alertBox.textContent = err.message || 'Error updating doctor.';
                alertBox.classList.remove('d-none');
            }
        });
    }
}

async function loadAdminPatients() {
    const tbody = document.getElementById('adminPatientsBody');
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><span class="spinner-border spinner-border-sm"></span> Loading patients...</td></tr>';

    try {
        const patients = await API.get('/admin/patients');
        tbody.innerHTML = '';
        if (patients.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No registered patients.</td></tr>';
            return;
        }

        patients.forEach(p => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${p.patientId}</td>
                <td class="fw-bold">${p.name}</td>
                <td>${p.email}</td>
                <td>${p.mobile}</td>
                <td>
                    <div>${p.age} / ${p.gender}</div>
                    ${p.bloodGroup ? `<span class="badge bg-danger-subtle text-danger border border-danger-subtle small mt-1">Blood: ${p.bloodGroup}</span>` : ''}
                </td>
                <td>
                    <div class="small">${p.address || 'N/A'}</div>
                    ${p.medicalHistory ? `<small class="text-muted d-block mt-1 text-truncate" style="max-width: 200px;">History: ${p.medicalHistory}</small>` : ''}
                </td>
                <td>
                    <button class="btn btn-sm btn-outline-danger delete-pat-btn" data-id="${p.patientId}">
                        <i class="bi bi-trash me-1"></i>Delete
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        document.querySelectorAll('.delete-pat-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (confirm(`Are you sure you want to delete Patient #${id}? All related records will be removed.`)) {
                    try {
                        await API.delete(`/admin/patients/${id}`);
                        loadAdminPatients();
                    } catch (err) {
                        alert('Failed to delete patient: ' + err.message);
                    }
                }
            });
        });
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-danger py-4">Failed to load patients.</td></tr>';
    }
}

async function loadAdminAppointments() {
    const tbody = document.getElementById('adminAppointmentsBody');
    const filterSelect = document.getElementById('statusFilter');

    async function fetchList() {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><span class="spinner-border spinner-border-sm"></span> Loading appointments...</td></tr>';
        const status = filterSelect ? filterSelect.value : '';
        const url = status ? `/admin/appointments?status=${status}&size=50` : '/admin/appointments?size=50';

        try {
            const res = await API.get(url);
            tbody.innerHTML = '';
            if (!res.content || res.content.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No matching appointments.</td></tr>';
                return;
            }

            res.content.forEach(a => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>#${a.appointmentId}</td>
                    <td>
                        <div class="fw-bold">${a.patientName}</div>
                        <small class="text-muted">${a.patientMobile}</small>
                        ${a.patientBloodGroup ? `<span class="badge bg-danger-subtle text-danger ms-1">${a.patientBloodGroup}</span>` : ''}
                    </td>
                    <td>
                        <div class="fw-bold">${a.doctorName}</div>
                        <small class="text-muted">${a.doctorSpecialization}</small>
                    </td>
                    <td>${a.appointmentDate} at ${a.appointmentTime}</td>
                    <td>
                        <div>${a.reason || 'N/A'}</div>
                        ${a.doctorNotes ? `<small class="text-success d-block mt-1"><i class="bi bi-prescription me-1"></i>${a.doctorNotes}</small>` : ''}
                    </td>
                    <td><span class="badge badge-${a.status.toLowerCase()}">${a.status}</span></td>
                    <td>
                        <select class="form-select form-select-sm admin-status-select" data-id="${a.appointmentId}">
                            <option value="PENDING" ${a.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                            <option value="CONFIRMED" ${a.status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                            <option value="COMPLETED" ${a.status === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                            <option value="CANCELLED" ${a.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                        </select>
                    </td>
                `;
                tbody.appendChild(tr);
            });

            document.querySelectorAll('.admin-status-select').forEach(sel => {
                sel.addEventListener('change', async () => {
                    const id = sel.getAttribute('data-id');
                    const status = sel.value;
                    try {
                        await API.put(`/admin/appointments/${id}/status`, { status });
                    } catch (err) {
                        alert('Failed to update appointment: ' + err.message);
                        fetchList();
                    }
                });
            });
        } catch (err) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-danger py-4">Error loading appointments.</td></tr>';
        }
    }

    if (filterSelect) {
        filterSelect.addEventListener('change', fetchList);
    }
    fetchList();
}

async function loadAdminReports() {
    try {
        const [summary, riskReports] = await Promise.all([
            API.get('/admin/reports/summary'),
            API.get('/admin/reports/risk')
        ]);

        document.getElementById('repTotalPatients').textContent = summary.totalPatients;
        document.getElementById('repTotalDoctors').textContent = summary.totalDoctors;
        document.getElementById('repTotalAppointments').textContent = summary.totalAppointments;
        document.getElementById('repConfirmed').textContent = summary.confirmedAppointments;
        document.getElementById('repCompleted').textContent = summary.completedAppointments;
        document.getElementById('repCancelled').textContent = summary.cancelledAppointments;

        const tbody = document.getElementById('riskReportsBody');
        tbody.innerHTML = '';
        if (riskReports.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-3 text-muted">No risk reports submitted.</td></tr>';
            return;
        }

        riskReports.forEach(r => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${r.reportId}</td>
                <td class="fw-bold">${r.patientName}</td>
                <td><small>${r.symptoms}</small></td>
                <td><span class="risk-badge-${r.riskLevel.toLowerCase()}">${r.riskLevel}</span></td>
                <td><small>${r.result}</small></td>
                <td>${r.reportDate}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error('Error loading admin reports:', err);
    }
}

// ==========================================
// CSV Export Functions (Synopsis Module 6)
// ==========================================

function triggerCSVDownload(filename, csvContent) {
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', filename);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

async function downloadAppointmentsCSV() {
    try {
        const res = await API.get('/admin/appointments?size=1000');
        const list = res.content || [];
        if (list.length === 0) {
            alert('No appointment records found to export.');
            return;
        }

        const headers = ['Appointment ID', 'Patient Name', 'Patient Mobile', 'Blood Group', 'Doctor Name', 'Doctor Specialization', 'Date', 'Time', 'Reason', 'Doctor Notes', 'Status'];
        const rows = list.map(a => [
            a.appointmentId,
            `"${(a.patientName || '').replace(/"/g, '""')}"`,
            `"${(a.patientMobile || '').replace(/"/g, '""')}"`,
            `"${(a.patientBloodGroup || 'N/A').replace(/"/g, '""')}"`,
            `"${(a.doctorName || '').replace(/"/g, '""')}"`,
            `"${(a.doctorSpecialization || '').replace(/"/g, '""')}"`,
            a.appointmentDate,
            a.appointmentTime,
            `"${(a.reason || '').replace(/"/g, '""')}"`,
            `"${(a.doctorNotes || '').replace(/"/g, '""')}"`,
            a.status
        ]);

        const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
        triggerCSVDownload(`appointments_report_${new Date().toISOString().split('T')[0]}.csv`, csv);
    } catch (err) {
        alert('Failed to export appointments CSV: ' + err.message);
    }
}

async function downloadRiskReportsCSV() {
    try {
        const list = await API.get('/admin/reports/risk');
        if (!list || list.length === 0) {
            alert('No health risk assessment records found to export.');
            return;
        }

        const headers = ['Report ID', 'Patient Name', 'Reported Symptoms', 'Risk Level', 'Recommendation / Explanation', 'Report Date'];
        const rows = list.map(r => [
            r.reportId,
            `"${(r.patientName || '').replace(/"/g, '""')}"`,
            `"${(r.symptoms || '').replace(/"/g, '""')}"`,
            r.riskLevel,
            `"${(r.result || '').replace(/"/g, '""')}"`,
            r.reportDate
        ]);

        const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
        triggerCSVDownload(`risk_assessment_audit_${new Date().toISOString().split('T')[0]}.csv`, csv);
    } catch (err) {
        alert('Failed to export risk reports CSV: ' + err.message);
    }
}

async function downloadSummaryCSV() {
    try {
        const summary = await API.get('/admin/reports/summary');
        const headers = ['Metric', 'Value'];
        const rows = [
            ['Total Patients', summary.totalPatients],
            ['Total Doctors', summary.totalDoctors],
            ['Total Appointments Booked', summary.totalAppointments],
            ['Pending Appointments', summary.pendingAppointments],
            ['Confirmed Appointments', summary.confirmedAppointments],
            ['Completed Appointments', summary.completedAppointments],
            ['Cancelled Appointments', summary.cancelledAppointments],
            ['Total Risk Assessments', summary.totalRiskReports],
            ['Export Timestamp', new Date().toISOString()]
        ];

        const csv = [headers.join(','), ...rows.map(r => `"${r[0]}",${r[1]}`)].join('\n');
        triggerCSVDownload(`system_summary_report_${new Date().toISOString().split('T')[0]}.csv`, csv);
    } catch (err) {
        alert('Failed to export summary CSV: ' + err.message);
    }
}

async function downloadDatabaseBackup() {
    try {
        const token = API.getToken();
        const response = await fetch('/api/admin/database/backup', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!response.ok) {
            throw new Error(`Backup failed: ${response.statusText}`);
        }
        const sqlText = await response.text();
        const dateStr = new Date().toISOString().split('T')[0];
        triggerCSVDownload(`smartmedical_db_backup_${dateStr}.sql`, sqlText);
    } catch (err) {
        alert('Failed to download database backup: ' + err.message);
    }
}