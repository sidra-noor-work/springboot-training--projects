import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../Styles/Blogs.css';

function Blogs() {
  const [blogs, setBlogs] = useState([]);
  const [likedBlogs, setLikedBlogs] = useState([]);

  useEffect(() => {
    const fetchBlogs = async () => {
      try {
        const res = await axios.get('https://jsonplaceholder.typicode.com/posts?_limit=6');
        setBlogs(res.data);
      } catch (err) {
        console.error('Failed to fetch blogs:', err);
      }
    };

    fetchBlogs();
  }, []);

  const toggleLike = (id) => {
    setLikedBlogs((prev) =>
      prev.includes(id) ? prev.filter((blogId) => blogId !== id) : [...prev, id]
    );
  };

  return (
    <div className="container mt-5">
      <h2 className="mb-4 professional-heading text-center">Featured Blogs</h2>
      <div className="blog-list">
        {blogs.map((blog) => (
          <div className="blog-item d-flex flex-md-row flex-column align-items-start mb-4 p-4 shadow-sm rounded bg-white" key={blog.id}>
            <div className="blog-thumbnail me-md-4 mb-3 mb-md-0"></div>
            <div className="flex-grow-1">
              <h4 className="blog-title mb-2">{blog.title.slice(0, 50)}...</h4>
              <p className="blog-body mb-3 text-muted">{blog.body.slice(0, 120)}...</p>
              <button
                className={`btn like-button ${likedBlogs.includes(blog.id) ? 'liked' : ''}`}
                onClick={() => toggleLike(blog.id)}
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
