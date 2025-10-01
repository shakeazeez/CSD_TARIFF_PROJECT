// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState } from "react"; // React hooks for state management and side effects
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

export function Calculator({ onMenuClick }) {
  // ====================================
  // THEME INTEGRATION
  // ====================================

  // Get theme context for component-level color management
  const { colors, theme, toggleTheme, isDark } = useTheme();

  // Toast hook
  // ====================================
  // STATE VARIABLES
  // ====================================

  // Get backend URLs from environment variables (.env file)
  const backendURL = import.meta.env.VITE_BACKEND_URL;

  // Country data and user selections
  const [list, setList] = useState([]); // Array of all available countries from backend
  const [report, setReport] = useState(""); // Selected reporting country (importer)
  const [partner, setPartner] = useState(""); // Selected partner country (exporter)

  // Tariff calculation inputs
  const [hs, setHS] = useState(""); // HS Code (Harmonized System code for product classification)
  const [cost, setCost] = useState(); // Item cost in USD

  // API response data
  const [current, setCurrent] = useState([]); // Current tariff calculation results
  const [past, setPast] = useState([]); // Historical tariff data for charts

  // Loading states
  const [loadingCurrent, setLoadingCurrent] = useState(false);
  const [loadingPast, setLoadingPast] = useState(false);

  // Error and success messages
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Pinning
  const [pinned, setPinned] = useState([]);
  useEffect(() => {
    if (localStorage.getItem("authToken") != null) {
      const storedPins = localStorage.getItem("pin");
      if (storedPins) {
        try {
          // Try to parse as JSON array first (from login/addPin/delPin responses)
          const parsedPins = JSON.parse(storedPins);
          if (Array.isArray(parsedPins)) {
            setPinned(parsedPins.map(p => Number(p)));
          } else {
            // Fallback to comma-separated string format
            setPinned(storedPins.split(",").map((p) => Number(p.trim())));
          }
        } catch (e) {
          // If JSON parsing fails, try comma-separated format
          setPinned(storedPins.split(",").map((p) => Number(p.trim())));
        }
      }
    }
  }, []);

  const [fieldLabels, setFieldLabels] = useState({
    reportingCountry: "Reporting Country",
    partnerCountry: "Partner Country", 
    item: "Item",
    tariffRate: "Tariff Rate (%)",
    tariffAmount: "Tariff Amount (USD)",
    itemCostWithTariff: "Cost of Item including Tariff (USD)",
    tariffId: "IGNORE", 
    tariffDescription: "Tariff Description"
  });

  const [fieldValues, setFieldValues] = useState({
    reportingCountry: "",
    partnerCountry: "", 
    item: "",
    tariffRate: 0,
    tariffAmount: 0,
    itemCostWithTariff: 0,
    tariffId: 0,
    tariffDescription: ""
  });

  useEffect(() => {
    if (current && typeof current === 'object' && Object.keys(current).length > 0) {
      const newValues = {};
      Object.keys(fieldLabels).forEach(key => {
        if (current[key] !== undefined) {
          newValues[key] = current[key];
        }
      });
      
      if (Object.keys(newValues).length > 0) {
        setFieldValues(prev => ({ ...prev, ...newValues }));
      }
    }
  }, [current, fieldLabels]);

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

  // ====================================
  // DATA PROCESSING & TRANSFORMATION
  // ====================================

  // Transform country list for dropdown component compatibility
  // Converts backend format to {id, code} format expected by Dropdown component
  const modList =
    list && Array.isArray(list)
      ? list.map((item) => ({
          id: item.countryName, // Display name for dropdown
          code: item.countryName, // Value sent to backend
        }))
      : [];

  // Chart data preparation from historical tariff data
  // Extract tariff rates for chart visualization
  const value = [(past.tariffData || []).map((item) => item.tariffRate)];

  // Extract time periods for chart X-axis labels
  const labels = (past.tariffData || []).map((item) => item.startPeriod);

  // Chart legend (typically the item/product name)
  const legend = [past.item || "Tariff Rate"];

  // Chart title with country pair information
  const title =
    past.reportingCountry && past.partnerCountry
      ? `${past.reportingCountry} Import from ${past.partnerCountry} (in %)`
      : "Historical Tariff Trends";

  // ====================================
  // API REQUEST DATA TRANSFER OBJECTS (DTOs)
  // ====================================

  // DTO for current tariff calculation API call
  const tariffCalculationQueryDTO = {
    reportingCountry: report, // Country doing the importing
    partnerCountry: partner, // Country exporting the goods
    item: hs, // HS Code for product classification
    itemCost: cost, // Cost of the item in USD
  };

  // DTO for historical tariff data (overview) API call
  const tariffOverviewQueryDTO = {
    reportingCountry: report, // Country doing the importing
    partnerCountry: partner, // Country exporting the goods
    item: hs, // HS Code for product classification
  };

  // ====================================
  // UTILITY FUNCTIONS
  // ====================================

  // useEffect hook: Runs once when component mounts to fetch country data
  useEffect(() => {
    // Async function to fetch all available countries from backend
    const fetchCountry = async () => {
      try {
        // Make GET request to backend countries endpoint
        const response = await axios.get(`${backendURL}/tariff/countries`);

        // Update state with fetched country list
        setList(response.data);
      } catch (error) {
        console.error("Error fetching countries:", error);

        // FALLBACK DATA: Uncomment below for development/testing without backend
        // const fallbackCountries = [
        //     { countryName: "United States" },
        //     { countryName: "China" },
        //     { countryName: "Singapore" },
        //     { countryName: "Malaysia" },
        //     { countryName: "Japan" },
        //     { countryName: "South Korea" },
        //     { countryName: "Germany" },
        //     { countryName: "United Kingdom" },
        //     { countryName: "France" },
        //     { countryName: "Canada" }
        // ];
        // setList(fallbackCountries);
      }
    };

    // Execute the fetch function
    fetchCountry();
  }, []); // Empty dependency array = run only once on component mount

  // ====================================
  // USER ACTION HANDLERS
  // ====================================

  // Function to fetch current tariff calculation from backend
  const fetchCurrent = async () => {
    if (!report || !partner || !hs || !cost) {
      setError("Please fill in all fields before calculating.");
      return;
    }

    setLoadingCurrent(true);
    setError("");
    setSuccess("");

    try {
      // POST request to get current tariff calculation
      const response = await axios.post(
        `${backendURL}/tariff/current`,
        tariffCalculationQueryDTO
      );

      // Update state with current tariff results
      setCurrent(response.data);

      setSuccess("Tariff calculation completed successfully!");
    } catch (error) {
      console.error("Error fetching current tariff:", error);
      setError(
        error.response?.data?.message ||
          "This country combination for this item does not exists. Please check your inputs and try again."
      );
    } finally {
      setLoadingCurrent(false);
    }
  };

  // Function to fetch historical tariff data for chart visualization
  const fetchPast = async () => {
    if (!report || !partner || !hs) {
      setError(
        "Please fill in reporting country, partner country, and Item/Item Description before viewing historical data."
      );
      return;
    }

    setLoadingPast(true);
    setError("");
    setSuccess("");

    try {
      // POST request to get historical tariff data
      const response = await axios.post(
        `${backendURL}/tariff/past`,
        tariffCalculationQueryDTO
      );

      // Update state with historical tariff data
      setPast(response.data);

      setSuccess("Historical data loaded successfully!");
    } catch (error) {
      console.error("Error fetching historical tariff data:", error);
      setError(
        error.response?.data?.message ||
          "Unable to retrieve historical data. Please verify your inputs and try again."
      );
    } finally {
      setLoadingPast(false);
    }
  };

  // Function to add to pin
  const togglePin = (item) => {
    let updatedPins;
    if (pinned.includes(item)) {
      // Remove the item
      updatedPins = pinned.filter((p) => p !== item);
      delPin(item); // optional, if you handle backend
    } else {
      // Add the item
      updatedPins = [...pinned, item];
      addPin(item); // optional, if you handle backend
    }
    // Update state
    setPinned(updatedPins);
    // Sync to localStorage as JSON array (consistent with backend responses)
    localStorage.setItem("pin", JSON.stringify(updatedPins));
  };

  const addPin = async (item) => {
    try {
      const response = await axios.post(
        `${backendURL}/user/${localStorage.getItem(
          "username"
        )}/pinned-tariffs/${item}`,
        "",
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("authToken")}`,
          },
        }
      );
      localStorage.setItem("pin", JSON.stringify(response.data));
    } catch (error) {
      console.error("Error adding pin:", error);
    }
  };

  const delPin = async (item) => {
    try {
      const response = await axios.post(
        `${backendURL}/user/${localStorage.getItem(
          "username"
        )}/unpinned-tariffs/${item}`,
        "",
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("authToken")}`,
          },
        }
      );
      localStorage.setItem("pin", JSON.stringify(response.data));
    } catch (error) {
      console.error("Error removing pin:", error);
    }
  };

  // ====================================
  // COMPONENT RENDER (JSX)
  // ====================================

  return (
    <motion.div
      initial="hidden"
      animate="visible"
      variants={containerVariants}
      className="min-h-screen relative overflow-hidden"
      style={{
        background: "transparent",
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
              className="shadow-lg"
              style={{
                borderColor: colors.success,
                backgroundColor: `${colors.success}20`,
                borderWidth: "1px",
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
              className="shadow-lg"
              style={{
                borderColor: colors.error,
                backgroundColor: `${colors.error}20`,
                borderWidth: "1px",
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

      {/* MAIN CONTENT */}
      <div className="relative z-10 container mx-auto px-4 py-8">
        <motion.div
          variants={itemVariants}
          className="max-w-6xl mx-auto space-y-8"
        >
          {/* Page Header */}
          <motion.div variants={itemVariants} className="text-center mb-8">
            <h1
              className="text-4xl font-bold mb-4"
              style={{ color: colors.foreground }}
            >
              Tariff Calculator
            </h1>
            <p className="text-xl" style={{ color: colors.muted }}>
              Calculate international tariffs and analyze historical trends
            </p>
          </motion.div>

          {/* Calculator Form */}
          <motion.div variants={itemVariants}>
            <Card
              style={{
                backgroundColor: `${colors.surface}95`,
                borderColor: colors.border,
              }}
            >
              <CardHeader>
                <CardTitle style={{ color: colors.foreground }}>
                  <CalculatorIcon className="h-6 w-6 inline mr-2" />
                  Tariff Calculation
                </CardTitle>
                <CardDescription style={{ color: colors.muted }}>
                  Enter your trade details to calculate current tariffs
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                {/* Country Selection */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label
                      htmlFor="reporting-country"
                      style={{ color: colors.foreground }}
                    >
                      Reporting Country (Importer)
                    </Label>
                    <Dropdown
                      options={modList}
                      value={report}
                      onChange={(option) =>
                        setReport(option ? option.code : "")
                      }
                      placeholder="Select reporting country"
                      className="w-full"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label
                      htmlFor="partner-country"
                      style={{ color: colors.foreground }}
                    >
                      Partner Country (Exporter)
                    </Label>
                    <Dropdown
                      options={modList}
                      value={partner}
                      onChange={(option) =>
                        setPartner(option ? option.code : "")
                      }
                      placeholder="Select partner country"
                      className="w-full"
                    />
                  </div>
                </div>

                {/* HS Code and Cost */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label
                      htmlFor="hs-code"
                      style={{ color: colors.foreground }}
                    >
                      Item/Item Description
                    </Label>
                    <Input
                      id="hs-code"
                      type="text"
                      placeholder="Enter Item/Item Description (Slipper)"
                      value={hs}
                      onChange={(e) => setHS(e.target.value.toLowerCase())}
                      style={{
                        backgroundColor: colors.background,
                        borderColor: colors.border,
                        color: colors.foreground,
                      }}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="cost" style={{ color: colors.foreground }}>
                      Item Cost (USD)
                    </Label>
                    <Input
                      id="cost"
                      type="number"
                      placeholder="Enter item cost"
                      value={cost}
                      onChange={(e) => setCost(e.target.value)}
                      style={{
                        backgroundColor: colors.background,
                        borderColor: colors.border,
                        color: colors.foreground,
                      }}
                    />
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="flex flex-col sm:flex-row gap-4">
                  <Button
                    onClick={fetchCurrent}
                    disabled={loadingCurrent}
                    className="flex-1"
                    style={{
                      backgroundColor: colors.accent,
                      borderColor: colors.accent,
                      color: "#ffffff",
                    }}
                    onMouseEnter={(e) => {
                      e.target.style.backgroundColor = colors.hover;
                      e.target.style.color = "#ffffff";
                    }}
                    onMouseLeave={(e) => {
                      e.target.style.backgroundColor = `${colors.accent}`;
                      e.target.style.color = "#ffffff";
                    }}
                  >
                    {loadingCurrent ? (
                      <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                    ) : (
                      <CalculatorIcon className="h-4 w-4 mr-2" />
                    )}
                    Calculate Current Tariff
                  </Button>
                  <Button
                    onClick={fetchPast}
                    disabled={loadingPast}
                    variant="outline"
                    className="flex-1"
                    style={{
                      backgroundColor: colors.accent,
                      borderColor: colors.accent,
                      color: "#ffffff",
                    }}
                    onMouseEnter={(e) => {
                      e.target.style.backgroundColor = colors.hover;
                      e.target.style.color = "#ffffff";
                    }}
                    onMouseLeave={(e) => {
                      e.target.style.backgroundColor = `${colors.accent}`;
                      e.target.style.color = "#ffffff";
                    }}
                  >
                    {loadingPast ? (
                      <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                    ) : (
                      <TrendingUp className="h-4 w-4 mr-2" />
                    )}
                    View Historical Data
                  </Button>
                </div>
              </CardContent>
            </Card>
          </motion.div>

          {/* Current Tariff Results */}
          {current && Object.keys(current).length > 0 && (
            <motion.div variants={itemVariants}>
              <Card
                style={{
                  backgroundColor: `${colors.surface}95`,
                  borderColor: colors.border,
                }}
              >
                <CardHeader>
                  <CardTitle style={{ color: colors.foreground }}>
                    <Globe className="h-6 w-6 inline mr-2" />
                    Current Tariff Results
                  </CardTitle>
                  {/* add to pin button */}
                  {localStorage.getItem("authToken") != null ? (
                    <Button
                      className={`${
                        pinned.includes(Number(current.tariffId))
                          ? "w-20"
                          : "w-12"
                      }`}
                      onClick={() => togglePin(Number(current.tariffId))}
                      style={{
                        backgroundColor: pinned.includes(Number(current.tariffId))
                          ? colors.error
                          : colors.accent,
                        borderColor: pinned.includes(Number(current.tariffId))
                          ? colors.error
                          : colors.accent,
                        color: "#ffffff",
                      }}
                      onMouseEnter={(e) => {
                        e.target.style.backgroundColor = pinned.includes(Number(current.tariffId))
                          ? colors.error
                          : colors.hover;
                        e.target.style.color = "#ffffff";
                      }}
                      onMouseLeave={(e) => {
                        e.target.style.backgroundColor = pinned.includes(Number(current.tariffId))
                          ? colors.error
                          : colors.accent;
                        e.target.style.color = "#ffffff";
                      }}
                    >
                      {pinned.includes(Number(current.tariffId))
                        ? "Unpin"
                        : "Pin"}
                    </Button>
                  ) : null}
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {Object.entries(fieldLabels)
                      .filter(([key]) => key !== 'tariffId') // Exclude tariffId
                      .map(([key, label]) => (
                        <div
                          key={key}
                          className="p-4 rounded-lg"
                          style={{
                            backgroundColor: colors.background,
                            border: `1px solid ${colors.border}`,
                          }}
                        >
                          <div
                            className="text-sm font-medium mb-2"
                            style={{ color: colors.muted }}
                          >
                            {label}
                          </div>
                          <div
                            className="text-2xl font-bold"
                            style={{ color: colors.foreground }}
                          >
                            {typeof fieldValues[key] === "number" ? fieldValues[key].toFixed(2) : fieldValues[key] || "N/A"}
                          </div>
                        </div>
                      ))}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}

          {/* Historical Data Chart */}
          {past && past.tariffData && past.tariffData.length > 0 && (
            <motion.div variants={itemVariants}>
              <Card
                style={{
                  backgroundColor: `${colors.surface}95`,
                  borderColor: colors.border,
                }}
              >
                <CardHeader>
                  <CardTitle style={{ color: colors.foreground }}>
                    <TrendingUp className="h-6 w-6 inline mr-2" />
                    Historical Tariff Trends
                  </CardTitle>
                  <CardDescription style={{ color: colors.muted }}>
                    Line chart showing tariff rate changes over time
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <Chart
                    labels={labels}
                    value={value}
                    title={title}
                    legend={legend}
                  />
                </CardContent>
              </Card>
            </motion.div>
          )}

          {/* No Data Message */}
          {(!current || Object.keys(current).length === 0) &&
            (!past || !past.tariffData || past.tariffData.length === 0) && (
              <motion.div variants={itemVariants}>
                <Card
                  style={{
                    backgroundColor: `${colors.surface}95`,
                    borderColor: colors.border,
                  }}
                >
                  <CardContent className="text-center py-12">
                    <Globe
                      className="h-16 w-16 mx-auto mb-4"
                      style={{ color: colors.muted }}
                    />
                    <h3
                      className="text-xl font-semibold mb-2"
                      style={{ color: colors.foreground }}
                    >
                      Ready to Calculate
                    </h3>
                    <p style={{ color: colors.muted }}>
                      Fill in the form above and click "Calculate Current
                      Tariff" to get started
                    </p>
                  </CardContent>
                </Card>
              </motion.div>
            )}
        </motion.div>
      </div>
    </motion.div>
  );
}
