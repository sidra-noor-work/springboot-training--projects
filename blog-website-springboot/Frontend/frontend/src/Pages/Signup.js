
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../Styles/Signup.css';

// Configure axios defaults
axios.defaults.withCredentials = true;

function Signup() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');

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

  useEffect(() => {
    // Fetch CSRF token on component mount
    const fetchCsrfToken = async () => {
      try {
        await axios.get('http://localhost:8080/api/csrf', { withCredentials: true });
      } catch (err) {
        console.log('Error fetching CSRF token:', err);
      }
    };

    fetchCsrfToken();
  }, []);

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      const token = getCsrfTokenFromCookie();
      console.log("Sending to backend:", formData);

      const res = await axios.post('http://localhost:8080/auth/signup', formData, {
        withCredentials: true,
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': token
        },
      });

      console.log("Signup successful:", res.data);
      window.location.href = '/login';
    } catch (err) {
      console.error("Signup failed:", err);
      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Signup failed. Please try again.';
      setError(errorMsg);
    }
  };

  return (
    <div className="container d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <div className="card p-4 shadow" style={{ maxWidth: '400px', width: '100%' }}>
        <h2 className="text-center mb-4 text-primary-color">Welcome </h2>
        <h3 className="text-center mb-3">Sign Up</h3>
        {error && <div className="alert alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="username" className="form-label">Username</label>
            <input
              type="text"
              name="username"
              className="form-control"
              value={formData.username}
              onChange={handleChange}
              required
            />
          </div>

          <div className="mb-3">
            <label htmlFor="password" className="form-label">Password</label>
            <input
              type="password"
              name="password"
              className="form-control"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          <div className="d-grid">
            <button type="submit" className="btn btn-primary">Sign Up</button>
          </div>
        </form>

        <p className="text-center mt-3">
          Already have an account? <a href="/login">Login here</a>
        </p>
      </div>
    </div>
  );
}

export default Signup;