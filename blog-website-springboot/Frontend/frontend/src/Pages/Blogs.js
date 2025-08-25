import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Navigate } from 'react-router-dom';
import '../Styles/Blogs.css';

// Configure axios defaults
axios.defaults.withCredentials = true;

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

  // Function to get CSRF token from cookie
  const getCsrfTokenFromCookie = () => {
    const name = 'XSRF-TOKEN=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) === ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) === 0) {
        return c.substring(name.length, c.length);
      }
    }
    return '';
  };

  // Fetch CSRF token
  const fetchCsrfToken = async () => {
    try {
      await axios.get('http://localhost:8080/api/csrf', { withCredentials: true });
    } catch (err) {
      console.log('Error fetching CSRF token:', err);
    }
  };

  useEffect(() => {
    const initializeComponent = async () => {
      // Fetch CSRF token first
      await fetchCsrfToken();
      
      // Then fetch blogs
      axios
        .get('http://localhost:8080/blogs', { withCredentials: true })
        .then((res) => {
          if (res.data.success) {
            setBlogs(res.data.data);
            setIsAuthenticated(true);
          } else {
            setIsAuthenticated(false);
          }
          setAuthChecked(true);
        })
        .catch((err) => {
          console.error('Error fetching blogs:', err);
          setIsAuthenticated(false);
          setAuthChecked(true);
        });
    };

    initializeComponent();
  }, []);

  useEffect(() => {
    if (authChecked && !isAuthenticated && !alertShown) {
      alert('You must login first!!!');
      setAlertShown(true);
    }
  }, [authChecked, isAuthenticated, alertShown]);

  // Save liked blogs to localStorage
  useEffect(() => {
    localStorage.setItem('likedBlogs', JSON.stringify(likedBlogs));
  }, [likedBlogs]);

  const toggleLike = async (id) => {
    try {
      const token = getCsrfTokenFromCookie();
      
      // If you have a like endpoint on your backend, uncomment and modify this:
      /*
      await axios.post(`http://localhost:8080/blogs/${id}/like`, {}, {
        withCredentials: true,
        headers: {
          'X-XSRF-TOKEN': token
        }
      });
      */
      
      // For now, just update local state
      setLikedBlogs((prev) =>
        prev.includes(id)
          ? prev.filter((blogId) => blogId !== id)
          : [...prev, id]
      );
    } catch (err) {
      console.error('Error toggling like:', err);
      if (err.response?.status === 403) {
        // CSRF token might be expired, try to refresh it
        await fetchCsrfToken();
        setError('Session expired. Please refresh the page and try again.');
      } else {
        setError('Failed to update like status. Please try again.');
      }
    }
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
      {error && (
        <div className="alert alert-danger mb-3">
          {error}
        </div>
      )}
      <div className="blog-list">
        {blogs.map((blog) => (
          <div
            className="blog-item d-flex flex-md-row flex-column align-items-start mb-4 p-4 shadow-sm rounded bg-white"
            key={blog.id}
          >
            <div className="flex-grow-1">
              <h4 className="blog-title mb-2">{truncate(blog.title, 50)}</h4>
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