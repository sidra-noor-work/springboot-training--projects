import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Navigate } from 'react-router-dom';
import { FiEdit2, FiTrash2, FiX, FiCheck, FiLoader } from 'react-icons/fi';
import '../Styles/BlogForm.css'; // Import external CSS file

// Configure axios defaults
axios.defaults.withCredentials = true;

function BlogForm() {
  const [blogs, setBlogs] = useState([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [authChecked, setAuthChecked] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [csrfToken, setCsrfToken] = useState('');

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

  // Function to get JWT token from cookie or localStorage
  const getJwtTokenFromCookie = () => {
    // First try to get from cookie
    const name = 'jwt=';
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
    // If not in cookie, try localStorage
    return localStorage.getItem('token') || '';
  };

  // Function to refresh CSRF token
  const refreshCsrfToken = async () => {
    try {
      await axios.get('http://localhost:8080/api/csrf', { withCredentials: true });
      const token = getCsrfTokenFromCookie();
      setCsrfToken(token);
      return token;
    } catch (err) {
      console.log('Error refreshing CSRF token:', err);
      return '';
    }
  };

  // Fetch CSRF token
  const fetchCsrfToken = async () => {
    try {
      await axios.get('http://localhost:8080/api/csrf', { withCredentials: true });
      const token = getCsrfTokenFromCookie();
      setCsrfToken(token);
    } catch (err) {
      console.log('Error fetching CSRF token:', err);
    }
  };

  // Fetch blogs on mount
  useEffect(() => {
    const initializeComponent = async () => {
      // Always refresh CSRF token first, especially after login
      await refreshCsrfToken();
      await fetchBlogs();
    };
    initializeComponent();
  }, []);

  const fetchBlogs = async () => {
    setLoading(true);
    setError(null);
    try {
      // Ensure we have a fresh CSRF token before making authenticated requests
      let currentCsrfToken = getCsrfTokenFromCookie();
      if (!currentCsrfToken) {
        currentCsrfToken = await refreshCsrfToken();
      }

      const jwtToken = getJwtTokenFromCookie();
      const headers = {
        'Content-Type': 'application/json',
      };
      
      // Add Authorization header if JWT token exists
      if (jwtToken) {
        headers['Authorization'] = `Bearer ${jwtToken}`;
      }

      const res = await axios.get('http://localhost:8080/blogs', { 
        withCredentials: true,
        headers
      });

      if (res.data.success) {
        setBlogs(res.data.data);
        setIsAuthenticated(true);
      } else {
        setIsAuthenticated(false);
      }
    } catch (err) {
      console.error('Error fetching blogs:', err);
      if (err.response?.status === 401) {
        setError('Session expired. Please login again.');
        setIsAuthenticated(false);
      } else {
        setError('Failed to fetch blogs. Please try again.');
        setIsAuthenticated(false);
      }
    } finally {
      setLoading(false);
      setAuthChecked(true);
    }
  };

  // Create or update blog
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      let currentCsrfToken = getCsrfTokenFromCookie();
      
      // If no CSRF token found, refresh it
      if (!currentCsrfToken) {
        currentCsrfToken = await refreshCsrfToken();
      }
      
      const jwtToken = getJwtTokenFromCookie();
      
      const headers = {
        'Content-Type': 'application/json',
        'X-XSRF-TOKEN': currentCsrfToken
      };
      
      // Add Authorization header if JWT token exists
      if (jwtToken) {
        headers['Authorization'] = `Bearer ${jwtToken}`;
      }

      console.log('Request headers:', headers); // Debug log
      console.log('JWT Token:', jwtToken); // Debug log
      console.log('CSRF Token:', currentCsrfToken); // Debug log

      const config = {
        withCredentials: true,
        headers
      };

      if (editingId) {
        await axios.put(
          `http://localhost:8080/blogs/${editingId}`,
          { title, content },
          config
        );
        setEditingId(null);
        setSuccessMessage('Blog updated successfully!');
      } else {
        await axios.post(
          'http://localhost:8080/blogs',
          { title, content },
          config
        );
        setSuccessMessage('Blog created successfully!');
      }

      setTitle('');
      setContent('');
      await fetchBlogs();
    } catch (err) {
      console.error('Error saving blog:', err);
      if (err.response?.status === 403) {
        // CSRF token might be expired, try to refresh it
        const newToken = await refreshCsrfToken();
        if (newToken) {
          setError('Security token expired. Please try again.');
        } else {
          setError('Session expired. Please refresh the page.');
        }
      } else if (err.response?.status === 401) {
        setError('Authentication failed. Please login again.');
        setIsAuthenticated(false);
      } else {
        setError(err.response?.data?.message || 'Failed to save blog. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Delete blog
  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this blog?')) return;
    
    setLoading(true);
    setError(null);
    try {
      let currentCsrfToken = getCsrfTokenFromCookie();
      
      // If no CSRF token found, refresh it
      if (!currentCsrfToken) {
        currentCsrfToken = await refreshCsrfToken();
      }
      
      const jwtToken = getJwtTokenFromCookie();
      
      const headers = {
        'X-XSRF-TOKEN': currentCsrfToken
      };
      
      // Add Authorization header if JWT token exists
      if (jwtToken) {
        headers['Authorization'] = `Bearer ${jwtToken}`;
      }

      await axios.delete(`http://localhost:8080/blogs/${id}`, {
        withCredentials: true,
        headers
      });
      setSuccessMessage('Blog deleted successfully!');
      await fetchBlogs();
    } catch (err) {
      console.error('Error deleting blog:', err);
      if (err.response?.status === 403) {
        // CSRF token might be expired, try to refresh it
        const newToken = await refreshCsrfToken();
        if (newToken) {
          setError('Security token expired. Please try again.');
        } else {
          setError('Session expired. Please refresh the page.');
        }
      } else if (err.response?.status === 401) {
        setError('Authentication failed. Please login again.');
        setIsAuthenticated(false);
      } else {
        setError('Failed to delete blog. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Edit blog
  const handleEdit = (blog) => {
    setTitle(blog.title);
    setContent(blog.content);
    setEditingId(blog.id);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const cancelEdit = () => {
    setTitle('');
    setContent('');
    setEditingId(null);
  };

  if (!authChecked) {
    return (
      <div className="auth-check">
        <div className="spinner-container">
          <FiLoader className="spinner-icon" />
          <p>Checking authentication...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
  <div className="blog-form-container">
  {/* Header */}
  <header className="form-header">
    <h1>{editingId ? 'Edit Blog Post' : 'Create New Blog Post'}</h1>
    <p className="subtitle">
      {editingId ? 'Update your existing blog content' : 'Share your thoughts with the world'}
    </p>
  </header>

  {/* Error Message */}
  {error && (
    <div className="alert error">
      <p>{error}</p>
    </div>
  )}

  {/* Blog Form */}
  <div className="form-card">
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="title">Blog Title</label>
        <input
          type="text"
          id="title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Enter a compelling title"
          required
        />
      </div>
      
      <div className="form-group">
        <label htmlFor="content">Blog Content</label>
        <textarea
          id="content"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="Write your blog content here..."
          required
          rows="8"
        />
      </div>
      
      <div className="form-actions">
        <button
          type="submit"
          disabled={loading}
          className={editingId ? 'btn-warning' : 'btn-primary'}
        >
          {loading ? (
            <FiLoader className="spin" />
          ) : editingId ? (
            <>
              <FiCheck className="icon" />
              Update
            </>
          ) : (
            'Publish'
          )}
        </button>
        
        {editingId && (
          <button
            type="button"
            onClick={cancelEdit}
            disabled={loading}
            className="btn-secondary"
          >
            <FiX className="icon" />
            Cancel
          </button>
        )}
      </div>
    </form>
  </div>

  {/* Blog List */}
  <section className="blog-list-section">
    <h2>Your Blog Posts</h2>
    
    {loading && blogs.length === 0 ? (
      <div className="loading-container">
        <FiLoader className="spinner-icon" />
      </div>
    ) : blogs.length === 0 ? (
      <div className="empty-state">
        <p>You haven't created any blog posts yet.</p>
      </div>
    ) : (
      <div className="blog-list">
        {blogs.map((blog) => (
          <article key={blog.id} className="blog-card">
            <div className="blog-content">
              <h3>{blog.title}</h3>
              <p className="blog-text">{blog.content}</p>
              <div className="blog-actions">
                <button
                  onClick={() => handleEdit(blog)}
                  className="btn-edit"
                >
                  <FiEdit2 className="icon" />
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(blog.id)}
                  className="btn-delete"
                >
                  <FiTrash2 className="icon" />
                  Delete
                </button>
              </div>
            </div>
          </article>
        ))}
      </div>
    )}
  </section>

  {/* Success Message BELOW blog list */}
  {successMessage && (
    <div className="alert success" style={{ marginTop: '20px' }}>
      <p>{successMessage}</p>
    </div>
  )}

  {/* Debug Box at the very bottom */}
  <div style={{ backgroundColor: '#f5f5f5', padding: '10px', margin: '20px 0 0', fontSize: '12px' }}>
    <strong>Debug Info:</strong><br/>
    JWT Token: {getJwtTokenFromCookie() ? 'Present' : 'Missing'}<br/>
    CSRF Token: {getCsrfTokenFromCookie() ? 'Present' : 'Missing'}<br/>
    Auth Status: {isAuthenticated ? 'Authenticated' : 'Not Authenticated'}<br/>
    <button 
      onClick={async () => {
        const token = await refreshCsrfToken();
        if (token) {
          setSuccessMessage('CSRF token refreshed successfully!');
        } else {
          setError('Failed to refresh CSRF token');
        }
      }}
      style={{ marginTop: '5px', padding: '5px 10px', fontSize: '11px' }}
    >
      Refresh CSRF Token
    </button>
  </div>
</div>
);
}
export default BlogForm;