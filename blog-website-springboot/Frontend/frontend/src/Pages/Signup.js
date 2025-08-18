import React, { useState, useEffect } from 'react';
import api from '../Services/axiosConfig';
import '../Styles/Login.css';

function Signup() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Optional: clear previous session and ensure CSRF cookie
  useEffect(() => {
    api.post('/logout').catch(err => console.log('No active session to clear', err));
    api.get('/auth/csrf').catch(err => console.log('CSRF cookie set', err));
  }, []);
useEffect(() => {
  // Fetch CSRF cookie first
  api.get('/auth/csrf')
    .then(res => console.log('CSRF cookie set'))
    .catch(err => console.log('Failed to set CSRF cookie', err));

  // Optional: clear any previous session
  api.post('/logout').catch(err => console.log('No active session to clear', err));
}, []);

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/auth/signup', formData);

      if (response.status === 200 || response.status === 201) {
        setSuccess('Signup successful! Redirecting to login...');
        setTimeout(() => window.location.href = '/login', 2000);
      } else {
        setError('Signup failed: Unknown error');
      }
    } catch (err) {
      console.log(err.response);
      setError(`Signup failed: ${err.response?.data?.message || 'Unauthorized'}`);
    }
  };

  return (
    <div className="login-wrapper d-flex justify-content-center align-items-center vh-100">
      <div className="login-card p-4 shadow-lg">
        <h2 className="text-center mb-4 text-primary-color">Create Account</h2>
        {error && <div className="alert alert-danger">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group mb-3">
            <label className="form-label">Username</label>
            <input
              name="username"
              className="form-control custom-input"
              onChange={handleChange}
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
              required
            />
          </div>
          <button type="submit" className="btn btn-primary w-100">Signup</button>
        </form>

        <div className="text-center mt-3">
          <p>Already have an account? <a href="/login" className="text-link">Login here</a></p>
        </div>
      </div>
    </div>
  );
}

export default Signup;
