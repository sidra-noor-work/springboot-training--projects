// src/pages/BlogForm.js
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function BlogForm() {
  const [blogs, setBlogs] = useState([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const token = localStorage.getItem('token');

  // Redirect if no token
  useEffect(() => {
    if (!token) {
      alert('You must be logged in to view blogs.');
      window.location.href = '/login';
      return;
    }

    axios.get('http://localhost:8080/blogs', {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
      .then(res => {
        if (res.data.success) {
          setBlogs(res.data.data);
        } else {
          alert(res.data.message || 'Failed to load blogs.');
        }
      })
      .catch(err => {
        console.error('Error fetching blogs:', err);
        console.log('You must be logged in to view blogs.');
        window.location.href = '/login';
      });
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await axios.post('http://localhost:8080/blogs',
        { title, content },
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

      setTitle('');
      setContent('');
      window.location.reload(); // Refresh blogs
    } catch (err) {
      console.error('Error posting blog:', err);
      alert('Login required to create blog');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token'); // Clear token
    alert('Logged out successfully');
    window.location.href = '/login'; // Redirect
  };

  return (
    <div style={{ maxWidth: "600px", margin: "0 auto", padding: "1rem" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h2>Create a Blog</h2>
        <button onClick={handleLogout} style={{ background: "#dc3545", color: "white", padding: "0.5rem 1rem", border: "none", borderRadius: "5px", cursor: "pointer" }}>
          Logout
        </button>
      </div>

      <form onSubmit={handleSubmit} style={{ marginTop: "1rem" }}>
        <input
          type="text"
          value={title}
          onChange={e => setTitle(e.target.value)}
          placeholder="Enter blog title"
          required
          style={{ width: "100%", padding: "0.5rem", marginBottom: "1rem" }}
        />
        <textarea
          value={content}
          onChange={e => setContent(e.target.value)}
          placeholder="Enter blog content"
          required
          style={{ width: "100%", padding: "0.5rem", marginBottom: "1rem" }}
        />
        <button type="submit" style={{ background: "#007bff", color: "white", padding: "0.5rem 1rem", border: "none", borderRadius: "5px" }}>
          Post
        </button>
      </form>

      <h3 style={{ marginTop: "2rem" }}>All Blogs</h3>
      <ul style={{ listStyle: "none", paddingLeft: 0 }}>
        {blogs.map((blog, idx) => (
          <li key={idx} style={{ marginBottom: "1rem", borderBottom: "1px solid #ccc", paddingBottom: "1rem" }}>
            <strong>{blog.title}</strong><br />
            <p>{blog.content}</p>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default BlogForm;
