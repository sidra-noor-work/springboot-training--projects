import React, { useState } from 'react';
import axios from 'axios';

function Signup() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      // Log payload before sending
      console.log("Sending to backend:", formData);

      const res = await axios.post('http://localhost:8080/auth/signup', formData, {
        headers: {
          'Content-Type': 'application/json',
        },
      });

      console.log("Signup successful:", res.data);
      window.location.href = '/login'; // redirect after signup
    } catch (err) {
      console.error("Signup failed:", err);

      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error || // from Spring Security
        'Signup failed. Please try again.';
      setError(errorMsg);
    }
  };

  return (
    <div>
      <h2>Sign Up</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        Username: <input name="username" value={formData.username} onChange={handleChange} required /><br />
        Password: <input type="password" name="password" value={formData.password} onChange={handleChange} required /><br />
        <button type="submit">Sign Up</button>
      </form>
      <p>Already have an account? <a href="/login">Login here</a></p>
    </div>
  );
}

export default Signup;
