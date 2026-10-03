document.addEventListener('DOMContentLoaded', () => {
    if (!API.requireAuth('PATIENT')) return;

    const user = API.getUser();
    const ageInput = document.getElementById('riskAge');
    if (ageInput && user.age) {
        ageInput.value = user.age;
    }

    const form = document.getElementById('riskAssessmentForm');
    const resultBox = document.getElementById('riskResultBox');
    const alertBox = document.getElementById('riskAlert');
    const evaluateBtn = document.getElementById('evaluateRiskBtn');

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            alertBox.classList.add('d-none');
            resultBox.classList.add('d-none');

            const age = parseInt(document.getElementById('riskAge').value, 10);
            const selectedSymptoms = [];
            document.querySelectorAll('input[name="symptoms"]:checked').forEach(cb => {
                selectedSymptoms.push(cb.value);
            });

            if (selectedSymptoms.length === 0) {
                alertBox.className = 'alert alert-warning';
                alertBox.textContent = 'Please select at least one symptom to evaluate.';
                alertBox.classList.remove('d-none');
                return;
            }

            evaluateBtn.disabled = true;
            evaluateBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Evaluating Rule Engine...';

            try {
                const report = await API.post('/risk-predictions', {
                    age: age,
                    symptoms: selectedSymptoms
                });

                const badgeEl = document.getElementById('riskLevelBadge');
                badgeEl.className = `risk-badge-${report.riskLevel.toLowerCase()} fs-5`;
                badgeEl.textContent = `${report.riskLevel} RISK`;

                document.getElementById('riskExplanation').textContent = report.result;
                document.getElementById('riskReportDate').textContent = `Assessed on: ${report.reportDate}`;
                document.getElementById('reportedSymptoms').textContent = `Reported: ${report.symptoms}`;

                const consultBanner = document.getElementById('consultDoctorBanner');
                const bookVisitBtn = document.getElementById('bookVisitRiskBtn');
                if (report.riskLevel === 'HIGH' || report.riskLevel === 'MEDIUM') {
                    consultBanner.classList.remove('d-none');
                    if (bookVisitBtn) {
                        const reasonParam = encodeURIComponent(`Risk Assessment: ${report.riskLevel} Risk - Symptoms: ${report.symptoms}`);
                        bookVisitBtn.href = `/patient/book-appointment.html?reason=${reasonParam}&fromRisk=true`;
                    }
                } else {
                    consultBanner.classList.add('d-none');
                }

                resultBox.classList.remove('d-none');
                resultBox.scrollIntoView({ behavior: 'smooth' });
            } catch (err) {
                alertBox.className = 'alert alert-danger';
                alertBox.textContent = err.message || 'Error processing risk assessment.';
                alertBox.classList.remove('d-none');
            } finally {
                evaluateBtn.disabled = false;
                evaluateBtn.innerHTML = '<i class="bi bi-cpu me-2"></i>Calculate Risk Category';
            }
        });
    }

    if (document.getElementById('patientRiskHistoryBody')) {
        loadRiskHistory();
    }
});

async function loadRiskHistory() {
    const historyTbody = document.getElementById('patientRiskHistoryBody');
    try {
        const reports = await API.get('/patients/me/risk-reports');
        historyTbody.innerHTML = '';
        if (reports.length === 0) {
            historyTbody.innerHTML = '<tr><td colspan="5" class="text-center py-3 text-muted">No past risk assessments.</td></tr>';
            return;
        }

        reports.forEach(r => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${r.reportId}</td>
                <td><small>${r.symptoms}</small></td>
                <td><span class="risk-badge-${r.riskLevel.toLowerCase()}">${r.riskLevel}</span></td>
                <td><small>${r.result}</small></td>
                <td>${r.reportDate}</td>
            `;
            historyTbody.appendChild(tr);
        });
    } catch (err) {
        historyTbody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-3">Error loading history.</td></tr>';
    }
}