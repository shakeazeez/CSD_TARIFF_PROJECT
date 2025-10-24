// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState } from "react"; // React hooks for state management and side effects
import { useLocation } from "react-router-dom"; // Navigation hook to access location state
import axios from "axios"; // HTTP client for API requests

// Animation library for smooth transitions
import { motion, AnimatePresence } from "framer-motion";

// shadcn/ui components - Modern, accessible UI components
import { Button } from "../components/ui/button"; // Customizable button component
import { Input } from "../components/ui/input"; // Input field component
import { Label } from "../components/ui/label"; // Label component
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card"; // Card components

// Theme and icon components
import { useTheme } from "../contexts/ThemeContext.jsx"; // Custom theme context for component-level theming
import { useAuth } from "../contexts/AuthContext.jsx"; // Authentication context for user management
import {
  Calculator as CalculatorIcon,
  Menu,
  Sun,
  Moon,
  TrendingUp,
  Globe,
  ArrowRight,
  RefreshCw,
  AlertCircle,
  CheckCircle,
} from "lucide-react"; // SVG icons

// Custom components
import Dropdown from "../components/Dropdown.jsx"; // Custom dropdown component
import Chart from "../components/Chart.jsx"; // Custom chart component
import { Header } from "../components/Header.jsx"; // Header component
import { useToast } from "../hooks/use-toast";
import Searches from "../components/Searches.jsx"; // Search history component

// ====================================
// ANIMATION VARIANTS
// ====================================

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      duration: 0.6,
      staggerChildren: 0.1,
    },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.5,
      ease: "easeOut",
    },
  },
};

// ====================================
// CALCULATOR COMPONENT
// ====================================

export function Chat({ onMenuClick }) {
  // ====================================
  // THEME INTEGRATION
  // ====================================

  // Get theme context for component-level color management
  const { colors, theme, toggleTheme, isDark } = useTheme();

  // To receive the data that was passed during navigation
  const location = useLocation();

  // Get authentication context for user management
  const { isAuthenticated } = useAuth()

  // ====================================
  // STATE VARIABLES
  // ====================================

  const [input, setInput] = useState(""); // user's query
  const [history, setHistory] = useState([]); // array - query: response // merge guests and logged in user's searches?
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");

  // load from local storage first
  useEffect(() => {
    if (isAuthenticated) {
      // fetchChatHistory();
    } else {
      const guestChatHistory = localStorage.getItem("chatHistory"); // fetch guest's chat history from local storage first
      setHistory(guestChatHistory);
    }

    if (chatHistory) {
      setHistory(chatHistory);
    }
  }, [])

  const fetchQueryResult = async () => {
    
  }

  // // fetch chat history
  // const fetchChatHistory = async () => {
  //   try { // calls backend to get the chat history for logged in user
  //     const response = await axios.get(`${backendURL}/news/history`, // THE ENDPOINT DOESNT EXIST YET
  //       {
  //         headers: {
  //           Authorization: `Bearer ${localStorage.getItem("authToken")}`, 
  //         },
  //       }
  //     );

  //     setHistory(prev => ({
  //       ...prev,
  //       [query]: response.data, // depends on what backend returns, might need to change
  //     }))
  //   } catch (e) {
  //     console.error("Error fetching chat history:", e);
  //   }
  // }

  // // add chat history to backend

}