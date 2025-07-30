// src/pages/BlogForm.js
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function BlogForm() {
  const [blogs, setBlogs] = useState([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const token = localStorage.getItem('token');

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
      alert('You must be logged in to view blogs.');
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

  return (
    <div>
      <h2>Create a Blog</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={title}
          onChange={e => setTitle(e.target.value)}
          placeholder="Enter blog title"
          required
        />
        <br />
        <textarea
          value={content}
          onChange={e => setContent(e.target.value)}
          placeholder="Enter blog content"
          required
        />
        <br />
        <button type="submit">Post</button>
      </form>

      <h3>All Blogs</h3>
      <ul>
        {blogs.map((blog, idx) => (
          <li key={idx}>
            <strong>{blog.title}</strong><br />
            {blog.content}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default BlogForm;
