import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../Styles/Login.css';

// Configure axios defaults
axios.defaults.withCredentials = true;

function Login() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [csrfToken, setCsrfToken] = useState('');

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

  // Function to refresh CSRF token
  const refreshCsrfToken = async () => {
    try {
      await axios.get('http://localhost:8080/api/csrf', { withCredentials: true });
      const token = getCsrfTokenFromCookie();
      setCsrfToken(token);
      return token;
    } catch (err) {
      console.log('Error refreshing CSRF token:', err);
      return '';
    }
  };

  useEffect(() => {
    // Clear any existing session and fetch CSRF token
    const initializeAuth = async () => {
      try {
        // Clear existing session
        await axios.post('http://localhost:8080/logout', {}, { withCredentials: true })
          .catch(err => console.log('No active session to clear', err));
        
        // Fetch CSRF token
        await refreshCsrfToken();
      } catch (err) {
        console.log('Error initializing auth:', err);
      }
    };

    initializeAuth();
  }, []);

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      let token = getCsrfTokenFromCookie();
      
      // If no token found, try to refresh
      if (!token) {
        token = await refreshCsrfToken();
      }
      
      const response = await axios.post('http://localhost:8080/auth/login', formData, {
        withCredentials: true,
        headers: {
          'X-XSRF-TOKEN': token,
          'Content-Type': 'application/json'
        }
      });

      const jwtToken = response.data.token;
      if (jwtToken) {
        // Store JWT token in both cookie and localStorage
        document.cookie = `jwt=${jwtToken}; path=/; SameSite=Lax`;
        localStorage.setItem("token", jwtToken);
        
        // Refresh CSRF token after login for future requests
        await refreshCsrfToken();
        
        // Redirect to blog page
        window.location.href = '/allblogs';
      } else {
        setError("Login failed: No token received");
      }
    } catch (err) {
      console.error('Login error:', err);
      if (err.response?.status === 403) {
        // CSRF token might be expired, try to refresh and retry
        const newToken = await refreshCsrfToken();
        if (newToken) {
          setError("Session expired. Please try again.");
        } else {
          setError("Security token refresh failed. Please reload the page.");
        }
      } else {
        setError(err.response?.data?.message || 'Invalid username or password');
      }
    }
  };

  const handleGithubLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/github';
  };

  return (
    <div className="login-wrapper d-flex justify-content-center align-items-center vh-100">
      <div className="login-card p-4 shadow-lg">
        <h2 className="text-center mb-4 text-primary-color">Welcome Back</h2>
        <p className="text-center text-muted">Login to your blog dashboard</p>
        {error && <div className="alert alert-danger">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group mb-3">
            <label className="form-label">Username</label>
            <input
              name="username"
              className="form-control custom-input"
              onChange={handleChange}
              value={formData.username}
              required
            />
          </div>
          <div className="form-group mb-4">
            <label className="form-label">Password</label>
            <input
              type="password"
              name="password"
              className="form-control custom-input"
              onChange={handleChange}
              value={formData.password}
              required
            />
          </div>
          <button type="submit" className="btn btn-primary w-100">Login</button>
        </form>

        <div className="text-center mt-3">
          <p>Or</p>
          <button
            onClick={handleGithubLogin}
            className="btn btn-dark w-100"
          >
            <i className="fab fa-github me-2"></i> Login with GitHub
          </button>
        </div>

        <div className="text-center mt-3">
          <p>Don't have an account? <a href="/signup" className="text-link">Sign up here</a></p>
        </div>
      </div>
    </div>
  );
}

export default Login;