import { useState } from 'react'
import { BrowserRouter as Router, Routes, Route} from 'react-router-dom'
import './App.css'
import { Home } from './pages/Home.jsx'
import { TestPage } from './pages/TestPage.jsx'
import { NotFound } from './pages/NotFound.jsx'

function App() {

  return (
    <>
      <Router>
        <Routes>
          <Route path="/" element={<Home/>}/>
          <Route path="/test-page" element={<TestPage/>}/>

          {/* Catch all non-existant page. Leave as last*/}
          <Route path="*" element={<NotFound/>}/>
        </Routes>
      </Router>
    </>
  )
}

export default App
