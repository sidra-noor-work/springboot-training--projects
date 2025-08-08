import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../Styles/Blogs.css';
import { useNavigate } from 'react-router-dom';
 function Blogs() {
   const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('You must log in first!');
      navigate('/login', { replace: true });
    }
  }, [navigate]);
  const [blogs, setBlogs] = useState([]);
  const [likedBlogs, setLikedBlogs] = useState(() => {
    // Initialize from localStorage
    const saved = localStorage.getItem('likedBlogs');
    return saved ? JSON.parse(saved) : [];
  });
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchBlogs = async () => {
      try {
        const res = await axios.get('https://jsonplaceholder.typicode.com/posts?_limit=6');
        setBlogs(res.data);
      } catch (err) {
        console.error('Failed to fetch blogs:', err);
        setError('Failed to load blogs. Please try again later.');
      }
    };

    fetchBlogs();
  }, []);

  useEffect(() => {
    // Persist liked blogs in localStorage
    localStorage.setItem('likedBlogs', JSON.stringify(likedBlogs));
  }, [likedBlogs]);

  const toggleLike = (id) => {
    setLikedBlogs((prev) =>
      prev.includes(id) ? prev.filter((blogId) => blogId !== id) : [...prev, id]
    );
  };

  // Helper to truncate text nicely
  const truncate = (str, maxLen) =>
    str.length > maxLen ? str.slice(0, maxLen).trimEnd() + '...' : str;

  if (error) return <div className="error-message">{error}</div>;

  return (
    <div className="container mt-5">
      <h2 className="mb-4 professional-heading text-center">Featured Blogs</h2>
      <div className="blog-list">
        {blogs.map((blog) => (
          <div
            className="blog-item d-flex flex-md-row flex-column align-items-start mb-4 p-4 shadow-sm rounded bg-white"
            key={blog.id}
          >
            <div className="blog-thumbnail me-md-4 mb-3 mb-md-0">
              {/* Add image here if you want */}
            </div>
            <div className="flex-grow-1">
              <h4 className="blog-title mb-2">{truncate(blog.title, 50)}</h4>
              <p className="blog-body mb-3 text-muted">{truncate(blog.body, 120)}</p>
              <button
                className={`btn like-button ${likedBlogs.includes(blog.id) ? 'liked' : ''}`}
                onClick={() => toggleLike(blog.id)}
                aria-pressed={likedBlogs.includes(blog.id)}
              >
                {likedBlogs.includes(blog.id) ? '❤️ Liked' : '🤍 Like'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Blogs;
