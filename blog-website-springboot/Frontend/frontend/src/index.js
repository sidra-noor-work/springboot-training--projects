import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import reportWebVitals from './reportWebVitals';
import axios from 'axios';

// Axios global defaults
axios.defaults.withCredentials = true; 
axios.defaults.baseURL = 'http://localhost:8080';

// Automatically add CSRF token from cookie to every request
axios.interceptors.request.use((config) => {
  const getCookie = (name) => {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    if (match) return match[2];
    return null;
  };

  const csrfToken = getCookie('XSRF-TOKEN');
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken;
  }
  return config;
}, (error) => Promise.reject(error));

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>


    <App />
  </React.StrictMode>
);

reportWebVitals();
