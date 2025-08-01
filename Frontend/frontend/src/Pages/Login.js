import React, { useState } from 'react';
import axios from 'axios';

function Login() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/auth/login', formData);

      const token = response.data.token;
      if (token) {
        console.log('Token:', token);

        // ✅ Save JWT to cookie for Spring Boot to read using @CookieValue("jwt")
        document.cookie = `jwt=${token}; path=/; SameSite=Lax`;

        // (Optional) Still store in localStorage if needed on client side
        localStorage.setItem("token", token);

        // Redirect to blogs page
        window.location.href = '/blogs';
      } else {
        setError("Login failed: No token received");
      }
    } catch (err) {
      setError('Invalid username or password');
    }
  };

  return (
    <div>
      <h2>Login</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit}>
        Username: <input name="username" onChange={handleChange} required /><br />
        Password: <input type="password" name="password" onChange={handleChange} required /><br />
        <button type="submit">Login</button>
      </form>
      <p>Don't have an account? <a href="/signup">Sign up here</a></p>
    </div>
  );
}

export default Login;
