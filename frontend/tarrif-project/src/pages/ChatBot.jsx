// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState, useRef } from "react"; // React hooks for state management and side effects
import axios from "axios"; // HTTP client for API requests

// Animation library for smooth transitions
import { motion, AnimatePresence } from "framer-motion";

// shadcn/ui components - Modern, accessible UI components
import { Button } from "../components/ui/button.jsx"; // Customizable button component
import { Input } from "../components/ui/input.jsx"; // Input field component
import { Label } from "../components/ui/label.jsx"; // Label component
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../components/ui/card.jsx"; // Card components

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
  Bot,
  Mic,
  MicOff,
  Send,
  MessageCircle,
  Clock,
  Plus,
  ExternalLink
} from "lucide-react"; // SVG icons

// Custom components
import { Header } from "../components/Header.jsx"; // Header component
import { useToast } from "../hooks/use-toast.js";

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

export function ChatBot({ onMenuClick }) {
  // Get authentication context for user management
  const { isAuthenticated } = useAuth();

  // Get backend URL from environment variables (.env file)
  const backendURL = import.meta.env.VITE_BACKEND_URL;

  // ====================================
  // THEME INTEGRATION
  // ====================================

  // Get theme context for component-level color management
  const { colors, theme, toggleTheme, isDark } = useTheme();

  // ====================================
  // STATE VARIABLES
  // ====================================

  const [input, setInput] = useState(""); // user's current query

  // for query history, in the form of array - query: response
  const [guestQueryHistory, setGuestQueryHistory] = useState([]); // for guest user
  const [UserQueryHistory, setUserQueryHistory] = useState([]); // for logged in user

  const [responseData, setResponseData] = useState({}); // for updating the response to backend (logged in user)

  const [currentQueryAndAnswer, setCurrentQueryAndAnswer] = useState(null); // currently displayed question and response

  const [expandedQueryIndex, setExpandedQueryIndex] = useState(null); // index of expanded question for answer display

  const [expandedSources, setExpandedSources] = useState(false); // for showing the sources (article url)

  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");

  // voice input functionality states
  const [isListening, setIsListening] = useState(false);
  const [isVoiceSupported, setIsVoiceSupported] = useState(false); // check if the browser supports speech recognition
  const [recognition, setRecognition] = useState(null); // holds the SpeechRecognition instance from the browser

  // ====================================
  // EFFECTS
  // ====================================

  // Auto-dismiss error message after 5 seconds
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(""), 5000);
      return () => clearTimeout(timer);
    }
  }, [error]);

  // Auto-dismiss success message after 5 seconds
  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(""), 5000);
      return () => clearTimeout(timer);
    }
  }, [success]);

  // load query history
  // check if browser supports speech recognition
  useEffect(() => {
    if (isAuthenticated) { // fetch from backend, endpoint not implemented yet
      // fetchChatHistory();
    } else {
      loadGuestQueryHistory();
    }
    // check for speech recognition support
    setIsVoiceSupported('webkitSpeechRecognition' in window || 'SpeechRecognition' in window);
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      // update backend endpoint
      // updateQueryToBackend();
    }
  }, [guestQueryHistory]);

  /*
   * Laods the guest user's query history from local storage and updates the queryHistory state
   * Called from the useEffect on component mount
   */
  const loadGuestQueryHistory = () => {
    const guestQueryHistory = localStorage.getItem("guestQueryHistory");

    if (guestQueryHistory) {
      try {
        setGuestQueryHistory(JSON.parse(guestQueryHistory));
      } catch (error) {
        console.log("Error parsing query history from local storage: {}", error);
        setGuestQueryHistory([]);
      }
    }
  }

  // // fetch chat history
  // 1. call the backend endpoint to fetch the historical query data
  // 2. save the response data in queryhistorystate and local storage
  // const fetchChatHistory = async () => {
  //   try { // calls backend to get the chat history for logged in user
  //     const response = await axios.get(`${backendURL}/news/history`, // THE ENDPOINT DOESNT EXIST YET
  //       {
  //         headers: {
  //           Authorization: `Bearer ${localStorage.getItem("authToken")}`, 
  //         },
  //       }
  //     );

  // const filteredHistory = response.data.map(history => ({
  //     response: history.synthesizedAnswer,
  //     sources: history.articles.map(article => ({
  //       title: article.title,
  //       url: article.url
  //     }))
  //   }));

  //    localStorage.setItem("userQueryHistory", JSON.stringify(filteredHistory.data));

  //     setUserQueryHistory(filteredHistory)
  //   } catch (e) {
  //     console.error("Error fetching chat history:", e);
  //   }
  // }

  // add query history to backend
  // const updateQueryToBackend = async () => {
  // try {
  //   const response = await axios.post(`${backendURL}/news/history`, reponseData, // THE ENDPOINT DOESNT EXIST YET
  //     {
  //       headers: {
  //         Authorization: `Bearer ${localStorage.getItem("authToken")}`,
  //       },
  //     }
  //   );
  //   localStorage.setItem("userQueryHistory", JSON.stringify(response.data));
  //   setUserQueryHistory(response.data);
  // } catch (error) {
  //   console.error("Error adding query searches:", error);
  // }
  // }

  /*
   * Fetch the query response from backend and update the localStorage
   */
  const fetchQueryResult = async () => {

    if (!input.trim()) {
      setError("Please enter a question before submitting.");
      return;
    }

    console.log("fetchQueryResult started, current state:", input);

    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const response = await axios.post(
        `${backendURL}/news/process`,
        null, // no request body
        { params: { query: input } } // query string
      );

      // extract the list of article sources
      const sources = response.data.articles.map(article => ({
        title: article.title,
        url: article.url
      }));

      const newQueryAndAnswer = { query: input, response: response.data.synthesizedAnswer, sources };

      console.log("newEntry: {}", newQueryAndAnswer);

      setCurrentQueryAndAnswer(newQueryAndAnswer);
      setExpandedQueryIndex(null); // clear highlighted history when new query is made
      setExpandedSources(false); // reset source expansion

      // update query history in local storage and history state
      setGuestQueryHistory(prev => {
        const updated = [...prev, newQueryAndAnswer];

        localStorage.setItem("guestQueryHistory", JSON.stringify(updated));
        // for logged in user, need to update backend too!!!, this is triggered by useEffect [queryHistory]

        return updated;
      });

      setSuccess("Query successful!");
      setInput("");
      // setLatestQueryAndAnswer("");
    } catch (error) {
      setError(error.response?.data?.message || "Query failed");
      console.error("Error state: {}", error);
    } finally {
      setLoading(false);
    }
  };

  /*
   * To select a query history when user click on the right sidebar
   */
  const handleQueryHistoryClick = (index) => {
    setExpandedQueryIndex(expandedQueryIndex === index ? null : index); // to hightlight the selected history

    if (guestQueryHistory[index]) {
      setCurrentQueryAndAnswer(guestQueryHistory[index]); // replace the current query display with selected query history
      setExpandedSources(false); // reset article url expansion
    }
  }

  /*
   * For expanding and collapsing the source part
   */
  const toggleSourceExpansion = () => {
    setExpandedSources(prev => !prev);
  };

  /*
   * Voice recognition function for converting voice input to words
   */
  const handleVoiceRecognition = () => {
    if (!isVoiceSupported) { // check if the browser supports speech recognition
      return;
    }

    if (isListening && recognition) { // stop the process if the user clicks on the button twice
      recognition.stop();
      setIsListening(false);
      setRecognition(null);
      return;
    }

    const WindowSpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    const speechRecognition = new WindowSpeechRecognition();
    setRecognition(speechRecognition);

    speechRecognition.continuous = true; // speech recognition doesn't stop listening even when user stops speaking
    speechRecognition.interimResults = true; // to display the live result when user is speaking
    speechRecognition.lang = 'en-US';

    // event handlers
    speechRecognition.onstart = () => {
      setIsListening(true);
    };

    // Triggered when speech is detected.
    // Allows the user to see their input while they are speaking
    speechRecognition.onresult = (event) => { // extract speech recognition results
      let userSpeech = ""; // stores finalised speech
      let interimTranscript = ""; // stores currently processed speech that might still change

      for (let i = 0; i < event.results.length; i++) {
        // for results[i][0]: 
        // [i] refers to the speech segment number
        // [0] refers to the browser's best guess for that segement
        const transcript = event.results[i][0].transcript;

        if (event.results[i].isFinal) { // make sure that the segment of speech (i) has been finished processed first
          userSpeech += transcript + " ";
        } else {
          interimTranscript += transcript; // shows the live speech
        }
      }

      // update input with final + interim text, keep listening until user manually stops (clicks the button again)
      setInput((userSpeech + interimTranscript).trim());
    };

    // triggered when there's an error with speech recognition
    speechRecognition.onerror = (event) => {
      // console.error('Speech recognition error:', event.error);
      setIsListening(false);
      // setError('Voice recognition failed. Please try again.');
    };

    // triggered when speech recognition process ends, which is when user stopped it
    speechRecognition.onend = () => {
      setIsListening(false);
      setRecognition(null); // clear the reference when recognition ends
    };

    // Start recognition
    speechRecognition.start();
  };

  return (
    <motion.div
      initial="hidden"
      animate="visible"
      variants={containerVariants}
      className="min-h-screen relative overflow-hidden"
      style={{
        background: 'transparent',
      }}
    >
      {/* TOP NAVIGATION */}
      <Header onMenuClick={onMenuClick} showUserInfo={true} />

      {/* NOTIFICATIONS */}
      <AnimatePresence>
        {success && (
          <motion.div
            initial={{ opacity: 0, y: -50 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -50 }}
            className="fixed top-20 right-4 z-50"
          >
            <Card
              className="shadow-lg border"
              style={{
                borderColor: colors.success,
                backgroundColor: colors.surface,
              }}
            >
              <CardContent className="flex items-center space-x-2 p-4">
                <CheckCircle
                  className="h-5 w-5"
                  style={{ color: colors.success }}
                />
                <span style={{ color: colors.foreground }}>{success}</span>
              </CardContent>
            </Card>
          </motion.div>
        )}
        {error && (
          <motion.div
            initial={{ opacity: 0, y: -50 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -50 }}
            className="fixed top-20 right-4 z-50"
          >
            <Card
              className="shadow-lg border"
              style={{
                borderColor: colors.error,
                backgroundColor: colors.surface,
              }}
            >
              <CardContent className="flex items-center space-x-2 p-4">
                <AlertCircle
                  className="h-5 w-5"
                  style={{ color: colors.error }}
                />
                <span style={{ color: colors.foreground }}>{error}</span>
              </CardContent>
            </Card>
          </motion.div>
        )}
      </AnimatePresence>

      {/* MAIN CONTENT AREA */}
      <div className="relative z-10 container mx-auto px-4 py-8">
        <div className="flex min-h-[80vh]">
          {/* LEFT CHAT AREA (75%) */}
          <div className="flex-1 flex flex-col">
            {/* CHAT MESSAGES CONTAINER */}
            <div
              className="flex-1 min-h-[500px] max-h-[70vh] overflow-y-auto p-6 space-y-6 rounded-t-2xl"
              style={{
                background: isDark ? `${colors.surface}95` : 'rgba(255, 255, 255, 0.95)',
                backdropFilter: 'blur(10px)',
                border: `1px solid ${colors.border}40`,
                scrollbarWidth: 'thin',
                scrollbarColor: isDark ? `${colors.accent}40 transparent` : `${colors.accent}60 transparent`,
                overflowY: 'auto'
              }}
            >
              {/* Welcome Message */}
              {!currentQueryAndAnswer && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  className="text-center py-12"
                >
                  <div
                    className="inline-flex items-center justify-center w-20 h-20 rounded-full mb-6"
                    style={{ backgroundColor: `${colors.accent}20` }}
                  >
                    <Bot className="w-10 h-10" style={{ color: colors.accent }} />
                  </div>
                  <h2
                    className="text-2xl font-bold mb-3"
                    style={{ color: colors.foreground }}
                  >
                    AI Assistant
                  </h2>
                  <p
                    className="text-lg max-w-md mx-auto"
                    style={{ color: colors.muted }}
                  >
                    Ask me anything about tariffs! I can help you with information and insights.
                  </p>
                </motion.div>
              )}

              {/* CURRENT QUERY AND ANSWER */}
              {currentQueryAndAnswer && (
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  variants={itemVariants}
                >
                  {/* User Message */}
                  <div className="flex justify-end mb-4">
                    <div
                      className="max-w-3xl rounded-2xl px-6 py-4 shadow-lg"
                      style={{
                        backgroundColor: colors.accent,
                        color: 'white',
                        backdropFilter: 'blur(10px)'
                      }}
                    >
                      <p className="text-sm font-medium">{currentQueryAndAnswer.query}</p>
                    </div>
                  </div>

                  {/* AI Response */}
                  <div className="flex justify-start">
                    <Card
                      className="max-w-4xl shadow-lg border"
                      style={{
                        backgroundColor: isDark ? `${colors.surface}90` : 'rgba(255, 255, 255, 0.9)',
                        backdropFilter: 'blur(10px)',
                        borderColor: `${colors.border}60`
                      }}
                    >
                      <CardHeader className="pb-3">
                        <div className="flex items-center space-x-2">
                          <Bot className="w-5 h-5" style={{ color: colors.accent }} />
                          <CardTitle className="text-sm" style={{ color: colors.foreground }}>
                            AI Assistant
                          </CardTitle>
                        </div>
                      </CardHeader>
                      <CardContent className="pt-0">
                        <p
                          className="text-sm leading-relaxed mb-4 chat-message"
                          style={{ color: colors.foreground }}
                        >
                          {currentQueryAndAnswer.response}
                        </p>

                        {/* Sources section */}
                        {Array.isArray(currentQueryAndAnswer.sources) && currentQueryAndAnswer.sources.length > 0 && (
                          <div className="mt-4 pt-4 border-t" style={{ borderColor: colors.border }}>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={toggleSourceExpansion}
                              className="flex items-center space-x-2 p-2 rounded-lg hover:bg-opacity-50 mb-3"
                              style={{
                                backgroundColor: expandedSources ? `${colors.accent}15` : 'transparent',
                                color: colors.foreground
                              }}
                            >
                              <ExternalLink className="w-4 h-4" style={{ color: colors.accent }} />
                              <span className="text-sm font-medium">
                                {currentQueryAndAnswer.sources.length} Source{currentQueryAndAnswer.sources.length > 1 ? 's' : ''}
                              </span>
                              <ArrowRight
                                className={`w-4 h-4 transition-transform duration-200 ${expandedSources ? 'rotate-90' : ''}`}
                                style={{ color: colors.muted }}
                              />
                            </Button>

                            <AnimatePresence>
                              {expandedSources && (
                                <motion.div
                                  initial={{ opacity: 0, height: 0 }}
                                  animate={{ opacity: 1, height: 'auto' }}
                                  exit={{ opacity: 0, height: 0 }}
                                  transition={{ duration: 0.2 }}
                                  className="overflow-hidden"
                                >
                                  <div className="grid grid-cols-1 gap-3">
                                    {currentQueryAndAnswer.sources.map((src, i) => (
                                      <motion.div
                                        key={i}
                                        initial={{ opacity: 0, y: -10 }}
                                        animate={{ opacity: 1, y: 0 }}
                                        transition={{ delay: i * 0.1 }}
                                      >
                                        <a
                                          href={src.url}
                                          target="_blank"
                                          rel="noopener noreferrer"
                                          className="block p-4 rounded-xl border transition-all duration-200 hover:shadow-md hover:scale-[1.02] group"
                                          style={{
                                            backgroundColor: isDark ? colors.background : '#ffffff',
                                            borderColor: colors.border,
                                            boxShadow: isDark ? 'none' : '0 1px 3px rgba(0,0,0,0.1)'
                                          }}
                                        >
                                          <div className="flex items-start space-x-3">
                                            <div
                                              className="flex-shrink-0 w-8 h-8 rounded-lg flex items-center justify-center"
                                              style={{ backgroundColor: `${colors.accent}20` }}
                                            >
                                              <ExternalLink className="w-4 h-4" style={{ color: colors.accent }} />
                                            </div>
                                            <div className="flex-1 min-w-0">
                                              <p
                                                className="text-sm font-medium mb-1 line-clamp-2 group-hover:text-opacity-80"
                                                style={{ color: colors.foreground }}
                                              >
                                                {src.title || 'Untitled Source'}
                                              </p>
                                              <p
                                                className="text-xs truncate"
                                                style={{ color: colors.muted }}
                                              >
                                                {new URL(src.url).hostname}
                                              </p>
                                            </div>
                                          </div>
                                        </a>
                                      </motion.div>
                                    ))}
                                  </div>
                                </motion.div>
                              )}
                            </AnimatePresence>
                          </div>
                        )}
                      </CardContent>
                    </Card>
                  </div>
                </motion.div>
              )}


            </div>

            {/* INPUT AREA */}
            <div
              className="border-t p-6 rounded-b-2xl"
              style={{
                backgroundColor: isDark ? `${colors.surface}95` : 'rgba(255, 255, 255, 0.95)',
                borderColor: colors.border,
                backdropFilter: 'blur(10px)'
              }}
            >
              <form
                onSubmit={e => { e.preventDefault(); fetchQueryResult(); }}
                className="max-w-4xl mx-auto"
              >
                <div className="flex items-end space-x-4">
                  <div className="flex-1">
                    <div
                      className="flex items-center space-x-3 p-4 rounded-2xl border shadow-sm transition-all duration-200 focus-within:ring-2"
                      style={{
                        backgroundColor: isDark ? colors.background : '#ffffff',
                        borderColor: colors.border,
                        boxShadow: isDark ? 'none' : '0 2px 8px rgba(0,0,0,0.1)',
                        ringColor: `${colors.accent}40`
                      }}
                    >
                      {/* Auto-resizing textarea styled to match Input */}
                      <textarea
                        id="chat-question"
                        ref={el => {
                          if (el) {
                            el.style.height = 'auto';
                            el.style.height = el.scrollHeight + 'px';
                          }
                        }}
                        placeholder="Type your question here..."
                        value={input}
                        onChange={e => {
                          setInput(e.target.value);
                          e.target.style.height = 'auto';
                          e.target.style.height = e.target.scrollHeight + 'px';
                        }}
                        onKeyDown={e => {
                          if (e.key === 'Enter' && !e.shiftKey) {
                            e.preventDefault();
                            if (!loading && input.trim()) fetchQueryResult();
                          }
                        }}
                        rows={1}
                        className={`border-0 bg-transparent focus-visible:ring-0 focus-visible:ring-offset-0 text-sm rounded-md shadow-none transition-all duration-200`}
                        style={{
                          color: colors.foreground,
                          width: '100%',
                          minHeight: '40px',
                          maxHeight: '200px',
                          background: 'transparent',
                          resize: 'none',
                          boxShadow: 'none',
                          border: 'none',
                          outline: 'none',
                          padding: 0,
                          fontFamily: 'inherit',
                          lineHeight: 1.5
                        }}
                        disabled={loading}
                        autoComplete="off"
                        spellCheck={true}
                      />

                      {/* Voice Button */}
                      {isVoiceSupported && (
                        <Button
                          type="button"
                          size="sm"
                          variant="ghost"
                          onClick={handleVoiceRecognition}
                          disabled={loading}
                          className={`p-2 rounded-full transition-all duration-200 ${isListening ? 'voice-pulse' : ''}`}
                          style={{
                            backgroundColor: isListening ? `${colors.accent}20` : 'transparent',
                            color: isListening ? colors.accent : colors.muted
                          }}
                        >
                          {isListening ? (
                            <Mic className="h-4 w-4" />
                          ) : (
                            <MicOff className="h-4 w-4" />
                          )}
                        </Button>
                      )}

                      {/* Send Button */}
                      <Button
                        type="submit"
                        size="sm"
                        disabled={loading || !input.trim()}
                        className="rounded-full p-2"
                        style={{
                          backgroundColor: colors.accent,
                          color: 'white',
                          opacity: loading || !input.trim() ? 0.5 : 1
                        }}
                      >
                        {loading ? (
                          <motion.div
                            animate={{ rotate: 360 }}
                            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                          >
                            <RefreshCw className="h-4 w-4" />
                          </motion.div>
                        ) : (
                          <Send className="h-4 w-4" />
                        )}
                      </Button>
                    </div>
                  </div>
                </div>
              </form>
            </div>
          </div>

          {/* RIGHT SIDEBAR - QUERY HISTORY (25%) */}
          <div
            className="w-80 ml-6 flex flex-col rounded-2xl"
            style={{
              backgroundColor: isDark ? `${colors.surface}95` : 'rgba(255, 255, 255, 0.95)',
              backdropFilter: 'blur(10px)',
              border: `1px solid ${colors.border}40`,
              minHeight: '500px',
              maxHeight: '70vh'
            }}
          >
            {/* Sidebar Header */}
            <div className="p-6 border-b rounded-t-2xl" style={{ borderColor: `${colors.border}60` }}>
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Clock className="w-5 h-5" style={{ color: colors.accent }} />
                  <h3
                    className="font-semibold text-lg"
                    style={{ color: colors.foreground }}
                  >
                    Chat History
                  </h3>
                </div>
                <Button
                  onClick={() => {
                    setCurrentQueryAndAnswer(null);
                    setInput("");
                    setError("");
                    setSuccess("");
                  }}
                  className="flex items-center gap-2 px-3 py-1 rounded-lg transition-all duration-200 hover:opacity-80"
                  style={{
                    backgroundColor: `${colors.accent}20`,
                    color: colors.accent
                  }}
                >
                  <Plus className="w-4 h-4" />
                  <span>New</span>
                </Button>
              </div>
              <p
                className="text-sm mt-1"
                style={{ color: colors.muted }}
              >
                Recent queries
                <br />
                Click on any question to revisit
              </p>
            </div>

            {/* History List */}
            <div
              className="flex-1 overflow-y-auto p-4 space-y-3 rounded-b-2xl"
              style={{
                scrollbarWidth: 'thin',
                scrollbarColor: isDark ? `${colors.accent}40 transparent` : `${colors.accent}60 transparent`,
                overflowY: 'auto'
              }}
            >
              {Array.isArray(guestQueryHistory) && guestQueryHistory.length > 0 ? (
                [...guestQueryHistory].reverse().map((item, reverseIdx) => {
                  const idx = guestQueryHistory.length - 1 - reverseIdx; // original index
                  return (
                    <motion.div
                      key={idx}
                      initial={{ opacity: 0, x: 20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ delay: reverseIdx * 0.05 }}
                    >
                      <Card
                        className={`transition-all duration-200 border cursor-pointer hover:shadow-lg hover:scale-[1.02] ${expandedQueryIndex === idx ? 'ring-2' : ''}`}
                        style={{
                          backgroundColor: expandedQueryIndex === idx
                            ? `${colors.accent}20`
                            : isDark ? `${colors.background}90` : 'rgba(255, 255, 255, 0.8)',
                          borderColor: expandedQueryIndex === idx ? colors.accent : `${colors.border}60`,
                          ringColor: expandedQueryIndex === idx ? colors.accent : 'transparent',
                          backdropFilter: 'blur(10px)',
                          boxShadow: expandedQueryIndex === idx
                            ? `0 4px 20px ${colors.accent}40`
                            : '0 2px 10px rgba(0,0,0,0.1)'
                        }}
                        onClick={() => handleQueryHistoryClick(idx)}
                      >
                        <CardContent className="p-4">
                          <div className="flex items-start space-x-3">
                            <div
                              className="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center"
                              style={{ backgroundColor: `${colors.accent}20` }}
                            >
                              <MessageCircle
                                className="w-4 h-4"
                                style={{ color: colors.accent }}
                              />
                            </div>
                            <div className="flex-1 min-w-0">
                              <p
                                className="text-sm font-medium line-clamp-2 leading-relaxed"
                                style={{ color: colors.foreground }}
                              >
                                {item.query}
                              </p>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    </motion.div>
                  );
                })
              ) : (
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <div
                    className="w-16 h-16 rounded-full flex items-center justify-center mb-4"
                    style={{ backgroundColor: `${colors.muted}20` }}
                  >
                    <MessageCircle
                      className="w-8 h-8"
                      style={{ color: colors.muted }}
                    />
                  </div>
                  <p
                    className="text-sm"
                    style={{ color: colors.muted }}
                  >
                    No conversations yet.
                  </p>
                  <p
                    className="text-xs mt-1"
                    style={{ color: colors.muted }}
                  >
                    Start by asking a question!
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}