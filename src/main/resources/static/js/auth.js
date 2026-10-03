document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const roleSelect = document.getElementById('loginRole');

    // Auto-select role if specified in URL or remembered
    const roleParam = urlParams.get('role') || localStorage.getItem('lastRole');
    if (roleSelect && roleParam) {
        roleSelect.value = roleParam;
    }
    if (roleSelect) {
        roleSelect.addEventListener('change', () => {
            localStorage.setItem('lastRole', roleSelect.value);
        });
    }

    // Check for session expired alert
    if (urlParams.get('error') === 'session_expired') {
        const alertBox = document.getElementById('loginAlert');
        if (alertBox) {
            alertBox.className = 'alert alert-warning';
            alertBox.textContent = 'Your session has expired. Please log in again.';
            alertBox.classList.remove('d-none');
        }
    } else if (urlParams.get('error') === 'unauthorized') {
        const alertBox = document.getElementById('loginAlert');
        if (alertBox) {
            alertBox.className = 'alert alert-info';
            alertBox.textContent = 'Please sign in to access this portal.';
            alertBox.classList.remove('d-none');
        }
    }

    // Login Form Handler
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('loginSubmitBtn');
            const alertBox = document.getElementById('loginAlert');
            alertBox.classList.add('d-none');

            const usernameOrEmail = document.getElementById('loginEmail').value.trim();
            const password = document.getElementById('loginPassword').value;
            const role = document.getElementById('loginRole').value;

            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Logging in...';

            try {
                const res = await API.post('/auth/login', { usernameOrEmail, password, role });
                API.setAuth(res.token, res);
                API.redirectRole(res.role);
            } catch (err) {
                alertBox.className = 'alert alert-danger';
                alertBox.textContent = err.message || 'Login failed. Please check your credentials.';
                alertBox.classList.remove('d-none');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Login';
            }
        });
    }

    // Register Form Handler
    const regForm = document.getElementById('registerForm');
    if (regForm) {
        regForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('regSubmitBtn');
            const alertBox = document.getElementById('regAlert');
            alertBox.classList.add('d-none');

            const payload = {
                name: document.getElementById('regName').value.trim(),
                email: document.getElementById('regEmail').value.trim(),
                password: document.getElementById('regPassword').value,
                mobile: document.getElementById('regMobile').value.trim(),
                age: parseInt(document.getElementById('regAge').value, 10),
                gender: document.getElementById('regGender').value,
                address: document.getElementById('regAddress').value.trim(),
                bloodGroup: document.getElementById('regBloodGroup')?.value || null,
                medicalHistory: document.getElementById('regMedicalHistory')?.value.trim() || null
            };

            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> Registering...';

            try {
                await API.post('/auth/register/patient', payload);
                alertBox.className = 'alert alert-success';
                alertBox.innerHTML = 'Registration successful! Redirecting to login...';
                alertBox.classList.remove('d-none');
                setTimeout(() => {
                    window.location.href = '/index.html?registered=true';
                }, 1500);
            } catch (err) {
                alertBox.className = 'alert alert-danger';
                alertBox.textContent = err.message || 'Registration failed.';
                alertBox.classList.remove('d-none');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Create Account';
            }
        });
    }
});