// src/App.js
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './Pages/Login';
import Signup from './Pages/Signup';
import BlogForm from './Pages/BlogForm';
import Navbar from './Components/Navbar';
import 'bootstrap/dist/css/bootstrap.min.css';
import '@fortawesome/fontawesome-free/css/all.min.css';
import Blogs from './Pages/Blogs';
function App() {
  return (
    <div>
      <Navbar/>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/navbar" element={<Navbar />} />
                <Route path="/blogs" element={<Blogs />} />
           <Route path="/createblog" element={<BlogForm />} />
      </Routes>
    </BrowserRouter>
    </div>
  );
}

export default App;
