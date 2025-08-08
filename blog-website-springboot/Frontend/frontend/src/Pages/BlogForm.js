// src/pages/BlogForm.js
import React, { useState, useEffect } from 'react';
import axios from 'axios';


function BlogForm() {
  const [blogs, setBlogs] = useState([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');

  useEffect(() => {
    // Attempt to load blogs, sending cookies automatically
    axios.get('http://localhost:8080/blogs', { withCredentials: true })
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
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await axios.post('http://localhost:8080/blogs',
        { title, content },
        { withCredentials: true }
      );

      setTitle('');
      setContent('');
      // Reload blogs after successful post
      const res = await axios.get('http://localhost:8080/blogs', { withCredentials: true });
      if (res.data.success) {
        setBlogs(res.data.data);
      }
    } catch (err) {
      console.error('Error posting blog:', err);
      alert('Login required to create blog');
      window.location.href = '/login';
    }
  };

  return (
    <div>


      <div style={{ maxWidth: "600px", margin: "0 auto", padding: "1rem" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <h2>Create a Blog</h2>
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
    </div>
  );
}

export default BlogForm;
