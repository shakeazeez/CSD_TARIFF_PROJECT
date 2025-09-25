import { useState } from 'react'
import { BrowserRouter as Router, Routes, Route} from 'react-router-dom'
import './App.css'
import { Home } from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Settings from './pages/Settings.jsx'
import Layout from './components/Layout.jsx'

function App() {

  return (
    <>
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<Home/>}/>
            <Route path="/login" element={<Login/>}/>
            <Route path="/register" element={<Register/>}/>
            <Route path="/dashboard" element={<Dashboard/>}/>
            <Route path="/settings" element={<Settings/>}/>

            {/* Catch all non-existant page. Leave as last*/}
            <Route path="*" element={<NotFound/>}/>
          </Routes>
        </Layout>
      </Router>
    </>
  )
}

export default App
