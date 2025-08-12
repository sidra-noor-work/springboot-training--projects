import React, { useState } from 'react';
import { useEffect } from 'react';
import axios from 'axios';
import '../Styles/Login.css';

function Login() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
useEffect(() => {
 
  axios.post('http://localhost:8080/logout', {}, { withCredentials: true })
    .catch(err => console.log('No active session to clear', err));
}, []);


  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/auth/login', formData);

      const token = response.data.token;
      if (token) {
        document.cookie = `jwt=${token}; path=/; SameSite=Lax`;
        localStorage.setItem("token", token);
        window.location.href = '/blogs';
      } else {
        setError("Login failed: No token received");
      }
    } catch (err) {
      setError('Invalid username or password');
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
          <p>Don’t have an account? <a href="/signup" className="text-link">Sign up here</a></p>
        </div>
      </div>
    </div>
  );
}

export default Login;
