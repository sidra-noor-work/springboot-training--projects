import React from 'react';
import '../Styles/Navbar.css';
import logo from '../Assets/icon.jpg';
import axios from 'axios';
const Navbar = () => {
const handleLogout = async () => {
  try {
    await axios.post('http://localhost:8080/auth/logout', {}, { withCredentials: true });
    localStorage.removeItem('token');
    alert('Logged out successfully');
    window.location.href = '/login';
  } catch (error) {
    console.error('Logout error:', error);
    alert('Logout failed, please try again.');
  }
};


  return (
    <nav className="navbar navbar-expand-lg navbar-dark custom-navbar px-3">
      <div className="container-fluid d-flex justify-content-between align-items-center">

        <div className="d-flex align-items-center">
          <a className="navbar-brand d-flex align-items-center me-3" href="#">
            <img src={logo} alt="App Logo" height="36" className="me-2 rounded-circle" />
            <span className="blog-title">Blog</span>
          </a>

          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarLeft"
            aria-controls="navbarLeft"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <i className="fas fa-bars"></i>
          </button>

        
        </div>

       
        <div className="d-flex align-items-center">
          <ul className="navbar-nav d-flex flex-row align-items-center">
            <li className="nav-item me-3">
              <a className="nav-link nav-theme" href="/createblog">Create</a>
            </li>
            <li className="nav-item me-3">
              <a className="nav-link nav-theme" href="/blogs"><i className="fas fa-bookmark pe-2"></i>All</a>
            </li>
            <li className="nav-item me-3">
              <a className="nav-link nav-theme" href="/login">Log In</a>
            </li>
            <li className="nav-item">
              <button onClick={handleLogout} className="btn btn-theme-outline">
                Logout
              </button>
            </li>
          </ul>
        </div>

      </div>
    </nav>
  );
};

export default Navbar;
