// utils/axiosConfig.js - Optional utility for global CSRF handling
import axios from 'axios';

// Configure axios defaults
axios.defaults.withCredentials = true;
axios.defaults.baseURL = 'http://localhost:8080';

// Function to get CSRF token from cookie
const getCsrfTokenFromCookie = () => {
  const name = 'XSRF-TOKEN=';
  const decodedCookie = decodeURIComponent(document.cookie);
  const ca = decodedCookie.split(';');
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) === 0) {
      return c.substring(name.length, c.length);
    }
  }
  return '';
};

// Request interceptor to automatically add CSRF token to requests
axios.interceptors.request.use(
  (config) => {
    // Add CSRF token to state-changing requests
    if (['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase())) {
      const csrfToken = getCsrfTokenFromCookie();
      if (csrfToken) {
        config.headers['X-XSRF-TOKEN'] = csrfToken;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle CSRF token expiration
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    // If we get a 403 (Forbidden) error, it might be due to CSRF token expiration
    if (error.response?.status === 403 && error.config && !error.config._retry) {
      error.config._retry = true;
      
      try {
        // Try to fetch a new CSRF token
        await axios.get('/api/csrf');
        
        // Retry the original request with the new token
        const csrfToken = getCsrfTokenFromCookie();
        if (csrfToken) {
          error.config.headers['X-XSRF-TOKEN'] = csrfToken;
        }
        
        return axios.request(error.config);
      } catch (csrfError) {
        // If we can't get a new CSRF token, redirect to login
        console.error('Failed to refresh CSRF token:', csrfError);
        window.location.href = '/login';
        return Promise.reject(error);
      }
    }
    
    return Promise.reject(error);
  }
);

export default axios;