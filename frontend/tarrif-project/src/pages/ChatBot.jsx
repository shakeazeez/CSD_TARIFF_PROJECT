// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState, useRef, useCallback } from "react"; // React hooks for state management and side effects
import api from "../lib/api.js"; // HTTP client for API requests

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
import { useTheme } from "../contexts/use-theme.js"; // Custom theme context for component-level theming
import { useAuth } from "../contexts/use-auth.js"; // Authentication context for user management
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

// ====================================
// CALCULATOR COMPONENT
// ====================================

export function ChatBot({ onMenuClick }) {
  // Get authentication context for user management
  const { isAuthenticated } = useAuth();

  // ====================================
  // THEME INTEGRATION
  // ====================================

  // Get theme context for component-level color management
  const { colors, isDark } = useTheme();

  // ====================================
  // STATE VARIABLES
  // ====================================

  const [input, setInput] = useState(""); // user's current query

  // for guest conversations: array of { id, title, messages: [{query,response,sources}], createdAt }
  const [guestConversations, setGuestConversations] = useState([]);
  const [activeConversationIndex, setActiveConversationIndex] = useState(0);
  const activeConversationRef = useRef(0);
  const currentQueryConversationIndexRef = useRef(null); // to track which conversation the current query belongs to
  // const [UserQueryHistory, setUserQueryHistory] = useState([]); // for logged in user

  // const [responseData, setResponseData] = useState({}); // for updating the response to backend (logged in user)

  const [currentQueryAndAnswer, setCurrentQueryAndAnswer] = useState(null); // currently displayed question and response

  // ref to the chat messages container for auto-scrolling
  const chatContainerRef = useRef(null);

  const [expandedQueryIndex, setExpandedQueryIndex] = useState(null); // index of expanded question for answer display

  const [expandedSources, setExpandedSources] = useState(false); // for showing the sources (article url)

  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");
  const [creatingNewConversation, setCreatingNewConversation] = useState(false); // guard to prevent spamming new chats
  const [deletingConversation, setDeletingConversation] = useState(false); // guard to prevent multiple delete calls
  const NEW_CONV_COOLDOWN_MS = 3000; // 3 second cooldown between new conversation creations
  const { toast } = useToast();

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

  // Helper to persist guest conversations to localStorage only for unauthenticated users
  const persistGuestConversations = useCallback((convs) => {
    try {
      if (!isAuthenticated) {
        localStorage.setItem("guestConversations", JSON.stringify(convs));
      }
    } catch { /* ignore storage errors */ }
  }, [isAuthenticated]);

  /*
   * Laods the guest user's query history from local storage and updates the queryHistory state
   * This helper is placed before the useEffect that references it to avoid temporal-dead-zone errors
   */
  const loadGuestQueryHistory = useCallback(() => {
    // Try new format first
    const stored = localStorage.getItem("guestConversations");
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        setGuestConversations(parsed);
        const idx = parsed.length - 1 >= 0 ? parsed.length - 1 : 0;
        setActiveConversationIndex(idx);
        activeConversationRef.current = idx;
        return;
      } catch (e) {
        console.warn("Failed to parse guestConversations", e);
      }
    }

    // Fallback: old flat history format (guestQueryHistory)
    const old = localStorage.getItem("guestQueryHistory");
    if (old) {
      try {
        const msgs = JSON.parse(old);
        const conv = {
          id: Date.now(),
          title: msgs && msgs.length > 0 ? (msgs[0].query || 'New Chat') : 'New Chat',
          messages: msgs,
          createdAt: new Date().toISOString()
        };
        setGuestConversations([conv]);
        setActiveConversationIndex(0);
        activeConversationRef.current = 0;
        try { persistGuestConversations([conv]); } catch { /* ignore */ }
        return;
      } catch (e) {
        console.warn("Failed to migrate old guestQueryHistory", e);
      }
    }

    // No history: initialize with one empty conversation
    const initial = [{ id: Date.now(), title: 'New Chat', messages: [], createdAt: new Date().toISOString() }];
    setGuestConversations(initial);
    setActiveConversationIndex(0);
    activeConversationRef.current = 0;
    try { persistGuestConversations(initial); } catch { /* ignore */ }
  }, [persistGuestConversations]);

  // fetch chat history from backend (for authenticated users)
  const fetchChatHistory = useCallback(async () => {
    try {
      const username = localStorage.getItem("username");
      if (!username) return;
      const response = await api.get(`/news/history/${username}`);

      // response is an array of conversation objects
      const convs = response.data.map(history => ({
        id: history.id,
        title: history.title,
        messages: history.messages.map(m => ({ query: m.query, response: m.response, sources: m.sources })),
        createdAt: history.createdAt
      }));

      setGuestConversations(convs);
      // For signed-in, set active to the latest (first in desc order)
      setActiveConversationIndex(0);
      activeConversationRef.current = 0;
    } catch (e) {
      console.error("Error fetching chat history:", e);
    }
  }, []);

  // load query history
  // check if browser supports speech recognition
  useEffect(() => {
    if (isAuthenticated) { // fetch from backend, endpoint not implemented yet
      fetchChatHistory();
    } else {
      loadGuestQueryHistory();
    }
    // check for speech recognition support
    setIsVoiceSupported('webkitSpeechRecognition' in window || 'SpeechRecognition' in window);
  }, [isAuthenticated, fetchChatHistory, loadGuestQueryHistory]);

  // Auto-scroll whenever history updates (new message or updated response)
  useEffect(() => {
    try {
      if (chatContainerRef.current) {
        chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
      }
    } catch {
      // Ignore scroll errors
    }
  }, [guestConversations]);

  useEffect(() => {
    if (isAuthenticated) {
      // update backend endpoint
      // updateQueryToBackend();
    }
  }, [guestConversations, isAuthenticated]);



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

    // Capture the conversation index for this query
    currentQueryConversationIndexRef.current = activeConversationRef.current;

    // Capture the previous completed entry (so we can send it as context)
    // Prefer the currently selected displayed conversation if it has a response
    // let previousEntry = null;
    // try {
    //   if (currentQueryAndAnswer && currentQueryAndAnswer.response) {
    //     previousEntry = currentQueryAndAnswer;
    //   } else {
    //     // Use the ref for the active conversation index here to avoid a stale state race
    //     const useIdx = (typeof activeConversationRef.current === 'number') ? activeConversationRef.current : (guestConversations.length - 1);
    //     const conv = (Array.isArray(guestConversations) && guestConversations.length > 0)
    //       ? guestConversations[(useIdx >= 0 && useIdx < guestConversations.length) ? useIdx : (guestConversations.length - 1)]
    //       : null;
    //     if (conv && conv.messages && conv.messages.length > 0) {
    //       for (let i = conv.messages.length - 1; i >= 0; i--) {
    //         if (conv.messages[i].response) { previousEntry = conv.messages[i]; break; }
    //       }
    //     }
    //   }
    // } catch (_) { previousEntry = currentQueryAndAnswer; }

    // Immediately create a pending message entry in the active conversation so the UI shows the user's message
    const displayedQuery = input;
    const pendingEntry = { query: displayedQuery, response: null, sources: [] };

    setGuestConversations(prev => {
      const updated = [...prev];
      const refIdx = (typeof currentQueryConversationIndexRef.current === 'number') ? currentQueryConversationIndexRef.current : -1;
      const idx = (refIdx >= 0 && refIdx < updated.length) ? refIdx : (updated.length - 1);
      // ensure conversation exists
      if (!updated[idx]) {
        updated[idx] = { id: Date.now(), title: 'New Chat', messages: [], createdAt: new Date().toISOString() };
      }
      updated[idx] = { ...updated[idx], messages: [...updated[idx].messages, pendingEntry] };
                        try { persistGuestConversations(updated); } catch {
                          // Ignore storage errors
                        }
      return updated;
    });

    // Make the pending entry the current displayed Q/A (so left panel highlights it)
    setCurrentQueryAndAnswer(pendingEntry);
    // clear input so it looks like the message was sent
    setInput("");
    // ensure container will scroll down after render

    try {
      // Build context from previous messages - only for guests
      let queryToSend = displayedQuery;
      if (!isAuthenticated) {
        try {
          const activeIdx = activeConversationRef.current;
          const conv = guestConversations[activeIdx];
          if (conv && Array.isArray(conv.messages) && conv.messages.length > 1) {
            // Include the last 3 previous completed messages as context (exclude the pending last one)
            const previousMessages = conv.messages.slice(0, -1);
            const last3Messages = previousMessages.slice(-3); // Get last 3 messages
            let contextParts = [];
            last3Messages.forEach(msg => {
              if (msg.query && msg.response) {
                contextParts.push(`User: ${msg.query}\nAssistant: ${msg.response}`);
              }
            });
            if (contextParts.length > 0) {
              queryToSend = `CONTEXT_START\n${contextParts.join('\n')}\nCONTEXT_END\nFollow-up: ${displayedQuery}`;
              console.log('Context built with last 3 messages:', contextParts.length, 'messages');
            }
          }
        } catch (e) {
          queryToSend = displayedQuery;
          console.log('Error building context:', e);
        }
      }

      const params = { query: queryToSend };
      if (isAuthenticated) {
        params.username = localStorage.getItem("username");
        // Add conversationId if appending to existing conversation
        const activeIdx = activeConversationRef.current;
        if (activeIdx >= 0 && guestConversations[activeIdx] && guestConversations[activeIdx].id) {
          params.conversationId = guestConversations[activeIdx].id;
        }
      }

      const response = await api.post('/news/process', null, { params });

      // Update local conversation id with server id for threading
      if (isAuthenticated && response.data.conversationId) {
        setGuestConversations(prev => {
          const updated = [...prev];
          const idx = activeConversationRef.current;
          if (idx >= 0 && updated[idx]) {
            updated[idx].id = response.data.conversationId;
          }
          return updated;
        });
      }

      // extract the list of article sources
      const sources = response.data.articles.map(article => ({ title: article.title, url: article.url }));

      // Update the last pending entry in the active conversation with the assistant response
      setGuestConversations(prev => {
        const updated = [...prev];
        const refIdx = (typeof currentQueryConversationIndexRef.current === 'number') ? currentQueryConversationIndexRef.current : -1;
        const idx = (refIdx >= 0 && refIdx < updated.length) ? refIdx : (updated.length - 1);
        if (!updated[idx]) return prev;
        const msgs = Array.isArray(updated[idx].messages) ? [...updated[idx].messages] : [];
        if (msgs.length === 0) {
          msgs.push({ query: displayedQuery, response: response.data.synthesizedAnswer, sources });
        } else {
          msgs[msgs.length - 1] = { query: displayedQuery, response: response.data.synthesizedAnswer, sources };
        }
        updated[idx] = { ...updated[idx], messages: msgs, title: updated[idx].title && updated[idx].title !== 'New Chat' ? updated[idx].title : (displayedQuery || updated[idx].title) };
  try { persistGuestConversations(updated); } catch {
    // Ignore storage errors
  }
        return updated;
      });

      const updatedEntry = { query: displayedQuery, response: response.data.synthesizedAnswer, sources };
      console.log("newEntry:", updatedEntry);

      setCurrentQueryAndAnswer(updatedEntry);
      setExpandedQueryIndex(null);
      setExpandedSources(false);

      if (isAuthenticated) {
        // refresh history from backend to include newly saved record
        fetchChatHistory();
      }

      setSuccess("Query successful!");
      setInput("");
      // setLatestQueryAndAnswer("");
    } catch (error) {
      setError(error.response?.data?.message || "Query failed");
      console.error("Error state: {}", error);
    } finally {
      setLoading(false);
      // scroll chat to bottom so the new assistant response (or loading) is visible
      setTimeout(() => {
        try {
          if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
          }
        } catch {
          // Ignore scroll errors
        }
      }, 50);
    }
  };

  /*
   * To select a query history when user click on the right sidebar
   */
  const handleQueryHistoryClick = (index) => {
    setExpandedQueryIndex(expandedQueryIndex === index ? null : index); // to highlight the selected history
    // index here refers to the reverse index in the displayed list (most recent first)
    // Convert to original conversation index
    const convIdx = guestConversations.length - 1 - index;
    if (Array.isArray(guestConversations) && guestConversations[convIdx]) {
      setActiveConversationIndex(convIdx);
      activeConversationRef.current = convIdx;
      const conv = guestConversations[convIdx];
      const lastMsg = conv.messages && conv.messages.length ? conv.messages[conv.messages.length - 1] : null;
      if (lastMsg) setCurrentQueryAndAnswer(lastMsg);
      setExpandedSources(false);
    }
  };

  const deleteConversation = (index) => {
    if (deletingConversation) return; // prevent multiple calls
    try {
      const doDelete = window.confirm('Delete this conversation? This cannot be undone.');
      if (!doDelete) return;

      setDeletingConversation(true);

      setGuestConversations(prev => {
        const updated = Array.isArray(prev) ? [...prev] : [];
        if (index < 0 || index >= updated.length) return prev;
        const convToDelete = updated[index];
        updated.splice(index, 1);

        if (updated.length === 0) {
          const initial = [{ id: Date.now(), title: 'New Chat', messages: [], createdAt: new Date().toISOString() }];
    try { persistGuestConversations(initial); } catch {
      // Ignore storage errors
    }
          activeConversationRef.current = 0;
          setActiveConversationIndex(0);
          setCurrentQueryAndAnswer(null);
          return initial;
        }

        // adjust active index/ref if needed
        let newActive = activeConversationRef.current;
        if (index === activeConversationRef.current) {
          newActive = Math.max(0, activeConversationRef.current - 1);
        } else if (index < activeConversationRef.current) {
          newActive = activeConversationRef.current - 1;
        }

        activeConversationRef.current = newActive;
        setActiveConversationIndex(newActive);
  try { persistGuestConversations(updated); } catch {
    // Ignore storage errors
  }

        const conv = updated[newActive];
        const lastMsg = conv && conv.messages && conv.messages.length ? conv.messages[conv.messages.length - 1] : null;
        setCurrentQueryAndAnswer(lastMsg);

        // If user is authenticated and conversation has an id (server-side), attempt server delete
        if (isAuthenticated && convToDelete && convToDelete.id) {
          const username = localStorage.getItem('username');
          const token = localStorage.getItem('authToken');
          if (username && token) {
            api.delete(`/news/history/${username}/${convToDelete.id}`)
              .then(() => {
                toast({ title: 'Deleted', description: 'Conversation deleted from server.' });
              })
              .catch(err => {
                console.warn('Server delete failed', err?.response?.status);
                if (err?.response?.status === 403) {
                  toast({ title: 'Not allowed', description: 'You are not authorized to delete this conversation.' });
                } else if (err?.response?.status === 404) {
                  toast({ title: 'Not found', description: 'Conversation not found on server (it may have already been removed).' });
                } else {
                  toast({ title: 'Error', description: 'Failed to delete conversation on server.' });
                }
              });
          }
        }

        return updated;
      });
    } catch (e) {
      console.error('Failed to delete conversation', e);
      setError('Failed to delete conversation');
    } finally {
      setDeletingConversation(false);
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
    speechRecognition.onerror = () => {
      // console.error('Speech recognition error');
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
              ref={chatContainerRef}
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
              {(!Array.isArray(guestConversations) || (guestConversations.length === 0) || !((guestConversations[activeConversationIndex >= 0 ? activeConversationIndex : (guestConversations.length - 1)] || {}).messages || []).length) && !currentQueryAndAnswer && (
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
                    {isAuthenticated
                      ? "Welcome back! Here is your chat history on the right. Do you have any questions about tariffs?"
                      : "Ask me anything about tariffs! I can help you with information and insights."
                    }
                  </p>
                </motion.div>
              )}

              {/* Render messages for the active conversation */}
              {Array.isArray(guestConversations) && guestConversations.length > 0 && (
                (() => {
                  const conv = guestConversations[activeConversationIndex >= 0 ? activeConversationIndex : (guestConversations.length - 1)];
                  const msgs = conv && Array.isArray(conv.messages) ? conv.messages : [];
                  return msgs.map((item, i) => (
                    <motion.div key={i} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.03 }}>
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
                          <p className="text-sm font-medium">{item.query}</p>
                        </div>
                      </div>

                      {/* Assistant / AI Response (or loading state) */}
                      <div className="flex justify-start mb-6">
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
                            {item.response ? (
                              <p className="text-sm leading-relaxed mb-4 chat-message" style={{ color: colors.foreground }}>
                                {item.response}
                              </p>
                            ) : (
                              <div className="mb-4">
                                <motion.div animate={{ opacity: [0.3, 1, 0.3] }} transition={{ duration: 1.5, repeat: Infinity, ease: "easeInOut" }} style={{ height: 12, width: '60%', background: `${colors.border}30`, borderRadius: 6, marginBottom: 8 }} />
                                <motion.div animate={{ opacity: [0.3, 1, 0.3] }} transition={{ duration: 1.5, repeat: Infinity, ease: "easeInOut", delay: 0.2 }} style={{ height: 12, width: '90%', background: `${colors.border}20`, borderRadius: 6, marginBottom: 6 }} />
                                <motion.div animate={{ opacity: [0.3, 1, 0.3] }} transition={{ duration: 1.5, repeat: Infinity, ease: "easeInOut", delay: 0.4 }} style={{ height: 12, width: '80%', background: `${colors.border}20`, borderRadius: 6 }} />
                                <div className="mt-2 text-xs text-muted-foreground" style={{ color: colors.muted }}>Generating answer...</div>
                              </div>
                            )}

                            {/* Sources for this entry */}
                            {Array.isArray(item.sources) && item.sources.length > 0 && (
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
                                    {item.sources.length} Source{item.sources.length > 1 ? 's' : ''}
                                  </span>
                                  <ArrowRight
                                    className={`w-4 h-4 transition-transform duration-200 ${expandedSources ? 'rotate-90' : ''}`}
                                    style={{ color: colors.muted }}
                                  />
                                </Button>

                                <AnimatePresence>
                                  {expandedSources && (
                                    <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} transition={{ duration: 0.2 }} className="overflow-hidden">
                                      <div className="grid grid-cols-1 gap-3">
                                        {item.sources.map((src, j) => (
                                          <motion.div key={j} initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: j * 0.05 }}>
                                            <a href={src.url} target="_blank" rel="noopener noreferrer" className="block p-4 rounded-xl border transition-all duration-200 hover:shadow-md hover:scale-[1.02] group" style={{ backgroundColor: isDark ? colors.background : '#ffffff', borderColor: colors.border, boxShadow: isDark ? 'none' : '0 1px 3px rgba(0,0,0,0.1)' }}>
                                              <div className="flex items-start space-x-3">
                                                <div className="flex-shrink-0 w-8 h-8 rounded-lg flex items-center justify-center" style={{ backgroundColor: `${colors.accent}20` }}>
                                                  <ExternalLink className="w-4 h-4" style={{ color: colors.accent }} />
                                                </div>
                                                <div className="flex-1 min-w-0">
                                                  <p className="text-sm font-medium mb-1 line-clamp-2 group-hover:text-opacity-80" style={{ color: colors.foreground }}>{src.title || 'Untitled Source'}</p>
                                                  <p className="text-xs truncate" style={{ color: colors.muted }}>{new URL(src.url).hostname}</p>
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
                  ));
                })()
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
                    try {
                      // Prevent spamming by checking if we are already creating a new conv
                      if (creatingNewConversation) {
                        toast({ title: 'Please wait', description: 'Creating a new conversation...' });
                        return;
                      }

                      // Cooldown check: compare last created time
                      const lastConv = Array.isArray(guestConversations) && guestConversations.length ? guestConversations[guestConversations.length - 1] : null;
                      if (lastConv && lastConv.createdAt) {
                        const lastMs = new Date(lastConv.createdAt).getTime();
                        if (Date.now() - lastMs < NEW_CONV_COOLDOWN_MS) {
                          toast({ title: 'Slow down', description: `You can create a new conversation every ${Math.ceil(NEW_CONV_COOLDOWN_MS/1000)}s` });
                          return;
                        }
                      }

                      setCreatingNewConversation(true);

                      // Create a new conversation and switch to it (don't delete old conversations)
                      const newConv = { id: isAuthenticated ? null : Date.now(), title: 'New Chat', messages: [], createdAt: new Date().toISOString() };
                      setGuestConversations(prev => {
                        const updated = Array.isArray(prev) ? [...prev, newConv] : [newConv];
                  try { persistGuestConversations(updated); } catch {
                    // Ignore storage errors
                  }
                        // set active index to the new conversation (last)
                        const newIdx = updated.length - 1;
                        setActiveConversationIndex(newIdx);
                        activeConversationRef.current = newIdx;
                        return updated;
                      });

                      setCurrentQueryAndAnswer(null);
                      setInput("");
                      setError("");
                      setSuccess("");
                      setExpandedQueryIndex(null);
                      setExpandedSources(false);
                      // scroll container to top
                      try { if (chatContainerRef.current) chatContainerRef.current.scrollTop = 0; } catch {
                        // Ignore scroll errors
                      }

                      // small delay to avoid rapid re-clicks, then re-enable
                      setTimeout(() => setCreatingNewConversation(false), NEW_CONV_COOLDOWN_MS);
                    } catch (e) {
                      setCreatingNewConversation(false);
                      console.error('Error creating new conversation', e);
                    }
                  }}
                  disabled={creatingNewConversation}
                  aria-busy={creatingNewConversation}
                  className="flex items-center gap-2 px-3 py-1 rounded-lg transition-all duration-200 hover:opacity-80"
                  style={{
                    backgroundColor: `${colors.accent}20`,
                    color: colors.accent,
                    opacity: creatingNewConversation ? 0.6 : 1
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
              {Array.isArray(guestConversations) && guestConversations.length > 0 ? (
                [...guestConversations].reverse().map((conv, reverseIdx) => {
                  const idx = guestConversations.length - 1 - reverseIdx; // original index
                  const lastMsg = conv.messages && conv.messages.length ? conv.messages[conv.messages.length - 1] : null;
                  const containerBg = expandedQueryIndex === idx
                    ? `${colors.accent}20`
                    : isDark ? `${colors.background}90` : 'rgba(255, 255, 255, 0.8)';
                  return (
                    <motion.div key={idx} initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: reverseIdx * 0.05 }}>
                      <Card
                        className={`transition-all duration-200 border cursor-pointer hover:shadow-lg hover:scale-[1.02] ${expandedQueryIndex === idx ? 'ring-2' : ''}`}
                        style={{
                          backgroundColor: containerBg,
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
                          <div className="flex items-start justify-between">
                            <div className="flex items-start space-x-3">
                              <div className="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center" style={{ backgroundColor: `${colors.accent}20` }}>
                                <MessageCircle className="w-4 h-4" style={{ color: colors.accent }} />
                              </div>
                              <div className="flex-1 min-w-0">
                                <p className="text-sm font-medium line-clamp-2 leading-relaxed" style={{ color: colors.foreground }}>
                                  {conv.title || (lastMsg ? lastMsg.query : 'New Chat')}
                                </p>
                              </div>
                            </div>
                            <div className="flex-shrink-0 ml-2">
                              <button
                                onClick={(e) => { e.stopPropagation(); deleteConversation(idx); }}
                                title="Delete conversation"
                                className="p-1 rounded-md hover:bg-red-100"
                                style={{ color: colors.muted }}
                              >
                                ✕
                              </button>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    </motion.div>
                  );
                })
              ) : (
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <div className="w-16 h-16 rounded-full flex items-center justify-center mb-4" style={{ backgroundColor: `${colors.muted}20` }}>
                    <MessageCircle className="w-8 h-8" style={{ color: colors.muted }} />
                  </div>
                  <p className="text-sm" style={{ color: colors.muted }}>No conversations yet.</p>
                  <p className="text-xs mt-1" style={{ color: colors.muted }}>Start by asking a question!</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}