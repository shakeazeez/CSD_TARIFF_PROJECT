import { useState } from 'react'
import { BrowserRouter as Router, Routes, Route} from 'react-router-dom'
import './App.css'
import { Home } from './pages/Home.jsx'
import { ThemeProvider } from './contexts/theme-provider'

function App() {

  return (
    <ThemeProvider defaultTheme="system" storageKey="tariff-ui-theme">
      <Router>
        <Routes>
          <Route path="/" element={<Home/>}/>
        </Routes>
      </Router>
    </ThemeProvider>
  )
}

export default App
