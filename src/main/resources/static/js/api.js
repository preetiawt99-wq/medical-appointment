// Global API helper and fetch wrapper
const API = {
    BASE_URL: '/api',

    getToken() {
        return localStorage.getItem('token');
    },

    getUser() {
        try {
            return JSON.parse(localStorage.getItem('user'));
        } catch (e) {
            return null;
        }
    },

    setAuth(token, user) {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
    },

    clearAuth() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    async request(endpoint, options = {}) {
        const url = `${this.BASE_URL}${endpoint}`;
        const headers = options.headers || {};

        if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
            headers['Content-Type'] = 'application/json';
        }

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, { ...options, headers });

            if (response.status === 401) {
                if (!endpoint.includes('/auth/')) {
                    const currentUser = this.getUser();
                    const roleParam = currentUser && currentUser.role ? `&role=${currentUser.role}` : '';
                    this.clearAuth();
                    window.location.href = `/index.html?error=session_expired${roleParam}`;
                    return null;
                }
            }

            const contentType = response.headers.get('content-type');
            let data = null;
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                const message = (data && data.message) ? data.message : `Error: ${response.statusText}`;
                throw new Error(message);
            }

            return data;
        } catch (error) {
            console.error(`API Error on [${options.method || 'GET'}] ${endpoint}:`, error);
            throw error;
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: body ? JSON.stringify(body) : null
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    },

    requireAuth(expectedRole) {
        const user = this.getUser();
        const token = this.getToken();
        if (!token || !user) {
            const roleParam = expectedRole ? `&role=${expectedRole}` : '';
            window.location.href = `/index.html?error=unauthorized${roleParam}`;
            return false;
        }

        if (expectedRole && user.role !== expectedRole) {
            alert('Access Denied: You do not have permission for this section.');
            this.redirectRole(user.role);
            return false;
        }
        return true;
    },

    redirectRole(role) {
        if (role === 'PATIENT') {
            window.location.href = '/patient/dashboard.html';
        } else if (role === 'DOCTOR') {
            window.location.href = '/doctor/dashboard.html';
        } else if (role === 'ADMIN') {
            window.location.href = '/admin/dashboard.html';
        } else {
            window.location.href = '/index.html';
        }
    },

    logout(role) {
        const lastRole = role || (this.getUser() ? this.getUser().role : null);
        this.clearAuth();
        if (lastRole) {
            window.location.href = `/index.html?role=${lastRole}`;
        } else {
            window.location.href = '/index.html';
        }
    }
};