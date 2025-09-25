import { useState } from 'react'
import { BrowserRouter as Router, Routes, Route} from 'react-router-dom'
import './App.css'

import { Home } from './pages/Home.jsx'
import { Login } from './pages/Login.jsx'
import { Register } from './pages/Register.jsx'

function App() {

  return (
    <>
      <Router>
        <Routes>
          <Route path="/" element={<Home/>}/>
          <Route path="/register" element={<Register/>}/> 
          <Route path="/login" element={<Login/>}/> 
        </Routes>
      </Router>
    </>
  )
}

export default App
