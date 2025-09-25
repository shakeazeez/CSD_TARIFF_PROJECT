import { useState } from 'react'
import { BrowserRouter as Router, Routes, Route} from 'react-router-dom'
import './App.css'
import { Login } from './pages/Login.jsx'
import { Home } from './pages/Home.jsx'

function App() {

  return (
    <>
      <Router>
        <Routes>
          <Route path="/" element={<Login/>}/> 
          <Route path="/" element={<Login/>}/> 
        </Routes>
      </Router>
    </>
  )
}

export default App
