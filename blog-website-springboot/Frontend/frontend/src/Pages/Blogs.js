import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Navigate } from 'react-router-dom';
import '../Styles/Blogs.css';

function Blogs() {
  const [blogs, setBlogs] = useState([]);
  const [likedBlogs, setLikedBlogs] = useState(() => {
    const saved = localStorage.getItem('likedBlogs');
    return saved ? JSON.parse(saved) : [];
  });
  const [error, setError] = useState(null);
  const [authChecked, setAuthChecked] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [alertShown, setAlertShown] = useState(false);

  // Static blogs
  const staticBlogs = [
    { id: 's1', title: 'The Rise of AI', content: 'Artificial Intelligence is shaping the future in unexpected ways.' },
    { id: 's2', title: 'Healthy Eating Tips', content: 'Maintain a balanced diet without giving up your favorites.' },
    { id: 's3', title: 'Travel Guide: Japan', content: 'Tokyo’s neon streets to Kyoto’s tranquil temples.' },
    { id: 's4', title: 'Mastering React', content: 'Best practices for modern, scalable web apps.' },
    { id: 's5', title: 'Financial Freedom 101', content: 'Budgeting and investing strategies for beginners.' },
    { id: 's6', title: 'The Science of Sleep', content: 'Why quality rest is vital for productivity.' },
    { id: 's7', title: 'Fitness Myths Busted', content: 'Separating fact from fiction in fitness.' },
    { id: 's8', title: 'History’s Greatest Inventions', content: 'From the wheel to the internet.' },
    { id: 's9', title: 'Minimalism Lifestyle', content: 'Declutter your space and mind.' },
    { id: 's10', title: 'Space Exploration Updates', content: 'Latest missions and discoveries.' }
  ];

  useEffect(() => {
    axios
      .get('http://localhost:8080/blogs', { withCredentials: true })
      .then((res) => {
        if (res.data.success) {
          setBlogs([...staticBlogs, ...res.data.data]);
          setIsAuthenticated(true);
        } else {
          setIsAuthenticated(false);
        }
        setAuthChecked(true);
      })
      .catch((err) => {
        console.error('Error fetching blogs:', err);
        setBlogs(staticBlogs); 
        setIsAuthenticated(false);
        setAuthChecked(true);
      });
  }, []);

  useEffect(() => {
    if (authChecked && !isAuthenticated && !alertShown) {
      alert('You must login first!!!');
      setAlertShown(true);
    }
  }, [authChecked, isAuthenticated, alertShown]);

  useEffect(() => {
    localStorage.setItem('likedBlogs', JSON.stringify(likedBlogs));
  }, [likedBlogs]);

  const toggleLike = (id) => {
    setLikedBlogs((prev) =>
      prev.includes(id)
        ? prev.filter((blogId) => blogId !== id)
        : [...prev, id]
    );
  };

  const truncate = (str, maxLen) =>
    str.length > maxLen ? str.slice(0, maxLen).trimEnd() + '...' : str;

  if (!authChecked) {
    return <div style={{ color: 'red' }}>Checking authentication...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (error) return <div className="error-message">{error}</div>;

  return (
    <div className="container mt-5">
      <h2 className="mb-4 professional-heading text-center">Featured Blogs</h2>
      <div className="blog-list">
        {blogs.map((blog) => (
          <div
            className="blog-item mb-4 shadow-sm rounded bg-white"
            key={blog.id}
          >
            {/* Blog Title Header */}
           <div className="blog-header p-3 rounded-top">

              <h4 className="m-0">{truncate(blog.title, 50)}</h4>
            </div>

            {/* Blog Content */}
            <div className="p-4">
              <p className="blog-body mb-3 text-muted">
                {truncate(blog.content || blog.body, 120)}
              </p>
              <button
                className={`btn like-button ${
                  likedBlogs.includes(blog.id) ? 'liked' : ''
                }`}
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
