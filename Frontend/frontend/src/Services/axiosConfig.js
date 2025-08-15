import axios from 'axios';

// Base Axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // send cookies (JWT + CSRF)
});

// Interceptor to automatically attach CSRF token from cookie
api.interceptors.request.use(config => {
  const csrfToken = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN='))
    ?.split('=')[1];

  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken;
  }

  return config;
}, error => Promise.reject(error));

export default api;
