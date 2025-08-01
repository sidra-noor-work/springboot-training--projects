import React, { useState } from 'react';
import axios from 'axios';
import '../Styles/Signup.css'; // Optional: for additional styles

function Signup() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      console.log("Sending to backend:", formData);

      const res = await axios.post('http://localhost:8080/auth/signup', formData, {
        headers: {
          'Content-Type': 'application/json',
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
