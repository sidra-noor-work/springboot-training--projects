import React, { useState, useEffect } from 'react';
import api from '../Services/axiosConfig';
import { FiEdit2, FiTrash2, FiX, FiCheck, FiLoader } from 'react-icons/fi';
import '../Styles/BlogForm.css';

function BlogForm() {
  const [blogs, setBlogs] = useState([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    fetchBlogs();
  }, []);

  const fetchBlogs = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/blogs');
      const blogsArray = Array.isArray(res.data) ? res.data : res.data.data;
      setBlogs(blogsArray || []);
    } catch (err) {
      console.error('Error fetching blogs:', err);
      setError(err.response?.data?.message || 'Failed to fetch blogs.');
      setBlogs([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      if (editingId) {
        await api.put(`/blogs/${editingId}`, { title, content });
        setSuccessMessage('Blog updated successfully!');
        setEditingId(null);
      } else {
        await api.post('/blogs', { title, content });
        setSuccessMessage('Blog created successfully!');
      }
      setTitle('');
      setContent('');
      await fetchBlogs();
    } catch (err) {
      console.error('Error saving blog:', err);
      setError(err.response?.data?.message || 'Failed to save blog. Login may be required.');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this blog?')) return;
    setLoading(true);
    setError(null);
    try {
      await api.delete(`/blogs/${id}`);
      setSuccessMessage('Blog deleted successfully!');
      await fetchBlogs();
    } catch (err) {
      console.error('Error deleting blog:', err);
      setError(err.response?.data?.message || 'Failed to delete blog.');
    } finally {
      setLoading(false);
    }
  };

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

  return (
    <div className="blog-form-container">
      <header className="form-header">
        <h1>{editingId ? 'Edit Blog Post' : 'Create New Blog Post'}</h1>
        <p className="subtitle">
          {editingId ? 'Update your existing blog content' : 'Share your thoughts with the world'}
        </p>
      </header>

      {error && <div className="alert error"><p>{error}</p></div>}
      {successMessage && <div className="alert success"><p>{successMessage}</p></div>}

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
            <button type="submit" disabled={loading} className={editingId ? 'btn-warning' : 'btn-primary'}>
              {loading ? <FiLoader className="spin" /> : editingId ? <><FiCheck className="icon" /> Update</> : 'Publish'}
            </button>
            {editingId && (
              <button type="button" onClick={cancelEdit} disabled={loading} className="btn-secondary">
                <FiX className="icon" /> Cancel
              </button>
            )}
          </div>
        </form>
      </div>

      <section className="blog-list-section">
        <h2>Your Blog Posts</h2>
        {loading && blogs.length === 0 ? (
          <div className="loading-container"><FiLoader className="spinner-icon" /></div>
        ) : Array.isArray(blogs) && blogs.length > 0 ? (
          <div className="blog-list">
            {blogs.map((blog) => (
              <article key={blog.id} className="blog-card">
                <div className="blog-content">
                  <h3>{blog.title}</h3>
                  <p className="blog-text">{blog.content}</p>
                  <div className="blog-actions">
                    <button onClick={() => handleEdit(blog)} className="btn-edit"><FiEdit2 className="icon" /> Edit</button>
                    <button onClick={() => handleDelete(blog.id)} className="btn-delete"><FiTrash2 className="icon" /> Delete</button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="empty-state"><p>You haven't created any blog posts yet.</p></div>
        )}
      </section>
    </div>
  );
}

export default BlogForm;
