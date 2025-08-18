import axios from 'axios';
import Cookies from 'js-cookie';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true, // needed for cookies from backend
});

api.interceptors.request.use(
  (config) => {
    const token = Cookies.get('jwt_token'); // Fixed: Changed from 'blogToken' to 'jwt_token'
    if (token) config.headers['Authorization'] = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;