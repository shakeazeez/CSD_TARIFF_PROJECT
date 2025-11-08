// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState, useCallback } from "react"; // React hooks for state management and side effects
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
    LetterTextIcon,
    Trash2,
    Plus,
    X,
} from "lucide-react"; // SVG icons

// Custom components
import Dropdown from "../components/Dropdown.jsx"; // Custom dropdown component
import Chart from "../components/Chart.jsx"; // Custom chart component
import { Header } from "../components/Header.jsx"; // Header component
import { useToast } from "../hooks/use-toast";

// ====================================
// HELPER FUNCTIONS AND CONSTANTS
// ====================================

// Preset data for development/testing
const presetListJSON = [
    {
        report: "United States",
        partner: ["Germany", "Canada", "Mexico"],
        hsCode: ["slipper", "boot", "sandal"],
        rate: [7, 5, 6]
    },
    {
        report: "Canada",
        partner: ["Mexico", "United States", "Germany"],
        hsCode: ["slipper", "boot", "sandal"],
        rate: [6, 4, 8]
    },
];

// Helper: normalize backend response into items shape used by UI
const normalizeToItems = (data) => {
    if (!data) return presetListJSON;
    // If already an array of items in the expected shape
    if (Array.isArray(data) && data.length && data[0].report) return data;
    // If response is an object that contains items list
    if (Array.isArray(data.items)) return data.items;
    if (Array.isArray(data.businessItems)) return data.businessItems;
    // If response has a map of report -> partners, convert (best-effort)
    if (typeof data === "object") {
        const arr = [];
        for (const key of Object.keys(data)) {
            if (Array.isArray(data[key].partner)) {
                arr.push({
                    report: key,
                    partner: data[key].partner,
                    hsCode: data[key].hsCode || data[key].hs || [],
                    rate: data[key].rate || []
                });
            }
        }
        if (arr.length) return arr;
    }
    return presetListJSON;
};

export function Business({onMenuClick}) {
    // ====================================
    // THEME INTEGRATION
    // ====================================

    // Get theme context for component-level color management
    const { colors } = useTheme();
    const backendURL = import.meta.env.VITE_BACKEND_URL || '';
    const { toast } = useToast();

    // auth / username resolution
    const { user } = useAuth?.() ?? {}; // gracefully handle if useAuth not available
    const username = user?.username || localStorage.getItem("username") || "demo";
    const token = localStorage.getItem("authToken");

    // Confirmation dialog state
    const [deleteConfirm, setDeleteConfirm] = useState(null); // { reportingCountry, partnerIndex, partnerName, hsCode }

    // Country data and user selections
    const [report, setReport] = useState(""); // Selected reporting country (importer)
    const [partner, setPartner] = useState(""); // Selected partner country (exporter)
    const [list, setList] = useState([]); // List of countries from backend
    // Tariff calculation inputs
    const [hs, setHS] = useState(""); // HS Code / item description

    // local items state (normalized to same shape used in UI)
    const [items, setItems] = useState(presetListJSON);

    // Fetch business items for the current user from backend: GET /business/{username}
    const fetchBusinessItems = useCallback(async () => {
        if (!username) return;
        try {
            const resp = await axios.get(`${backendURL}/business/${encodeURIComponent(username)}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            const data = resp?.data;
            const normalized = normalizeToItems(data);
            setItems(normalized);
        } catch (error) {
            console.error("Error fetching business items:", error);
            // fallback: keep existing items (or use preset)
            // setItems(presetListJSON);
        }
    }, [backendURL, username, token]);

    useEffect(() => {
        fetchBusinessItems();
    }, [fetchBusinessItems]);

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
        const fallbackCountries = [
            { countryName: "United States" },
            { countryName: "China" },
            { countryName: "Singapore" },
            { countryName: "Malaysia" },
            { countryName: "Japan" },
            { countryName: "South Korea" },
            { countryName: "Germany" },
            { countryName: "United Kingdom" },
            { countryName: "France" },
            { countryName: "Canada" }
        ];
        setList(fallbackCountries);
      }
    };

    // Execute the fetch function
    fetchCountry();
  }, [backendURL]); // run on mount / backendURL change


    const modList =
        list && Array.isArray(list)
            ? list.map((item) => ({
                id: item.countryName, // Display name for dropdown
                code: item.countryName, // Value sent to backend
            }))
            : [];

    // Add item: POST to /business/{username}/items
    const handleAddItem = async () => {
        if (!report || !partner || !hs) return;

        // fetch current rate from tariff service first (best-effort)
        let currentRate = 5; // default fallback
        try {
            const response = await axios.post(`${backendURL}/tariff/current`, {
                reportingCountry: report,
                partnerCountry: partner,
                item: hs,
                itemCost: 1000 // dummy cost, magic number
            });
            const fetched = response?.data;
            const rateFromResp = fetched?.tariffRate ?? fetched?.rate ?? fetched?.value ?? null;
            if (typeof rateFromResp === "number" && !Number.isNaN(rateFromResp)) {
                currentRate = rateFromResp;
            } else if (typeof rateFromResp === "string" && !Number.isNaN(Number(rateFromResp))) {
                currentRate = Number(rateFromResp);
            }
        } catch (err) {
            console.warn("Could not fetch tariff rate, using fallback rate:", err);
        }

        // Build payload expected by backend ReceiveListDTO: { information: [ ... ] }
        const payload = {
            information: [
                {
                    report,
                    partner,
                    hsCode: hs,
                    rate: currentRate
                }
            ]
        };

        try {
            await axios.post(`${backendURL}/business/${encodeURIComponent(username)}/items`, payload, {
                headers: { Authorization: `Bearer ${token}` }
            });
            // refresh from server after successful add
            await fetchBusinessItems();
            toast({
                title: "Item Added",
                description: `Successfully added ${hs} from ${report} → ${partner} (${currentRate}% rate)`,
                variant: "default",
            });
        } catch (err) {
            console.error("Error adding item to backend, falling back to local update:", err);
            // fallback: update local items state
            const newItems = items.map((it) => ({
                ...it,
                partner: Array.isArray(it.partner) ? [...it.partner] : [],
                hsCode: Array.isArray(it.hsCode) ? [...it.hsCode] : [],
                rate: Array.isArray(it.rate) ? [...it.rate] : [],
            }));
            let entry = newItems.find(item => item.report === report);
            if (entry) {
                entry.partner.push(partner);
                entry.hsCode.push(hs);
                entry.rate.push(currentRate);
            } else {
                newItems.push({
                    report,
                    partner: [partner],
                    hsCode: [hs],
                    rate: [currentRate]
                });
            }
            setItems(newItems);
            toast({
                title: "Item Added (Offline)",
                description: `Added ${hs} from ${report} → ${partner} (${currentRate}% rate) - local update`,
                variant: "default",
            });
        }

        // Clear inputs
        setHS("");
        setPartner("");
    };

    // Delete an item: call DELETE /business/{username}/items with body { information: [...] }
    const handleDelete = async (reportingCountry, partnerIndex) => {
        // find the item details from current state
        const entry = items.find(it => it.report === reportingCountry);
        if (!entry) return;
        const partnerName = entry.partner?.[partnerIndex];
        const hsCode = entry.hsCode?.[partnerIndex];

        if (!partnerName) return;

        // Show confirmation dialog instead of direct delete
        setDeleteConfirm({
            reportingCountry,
            partnerIndex,
            partnerName,
            hsCode
        });
    };

    // Confirm and execute delete
    const confirmDelete = async () => {
        if (!deleteConfirm) return;

        const { reportingCountry, partnerIndex, partnerName, hsCode } = deleteConfirm;

        const payload = {
            information: [
                {
                    report: reportingCountry,
                    partner: partnerName,
                    hsCode: hsCode
                }
            ]
        };

        try {
            // axios.delete with body requires { data: payload } as second arg
            await axios.delete(`${backendURL}/business/${encodeURIComponent(username)}/items`, { 
                data: payload,
                headers: { Authorization: `Bearer ${token}` }
            });
            // refresh from server after successful delete
            await fetchBusinessItems();
            toast({
                title: "Item Deleted",
                description: `Successfully deleted ${hsCode} from ${reportingCountry} → ${partnerName}`,
                variant: "default",
            });
        } catch (err) {
            console.error("Error deleting item on backend, falling back to local delete:", err);
            // fallback: local state update
            const newItems = items.map(item => {
                if (item.report === reportingCountry) {
                    return {
                        ...item,
                        partner: item.partner.filter((_, idx) => idx !== partnerIndex),
                        hsCode: item.hsCode.filter((_, idx) => idx !== partnerIndex),
                        rate: item.rate.filter((_, idx) => idx !== partnerIndex)
                    };
                }
                return item;
            }).filter(item => item.partner.length > 0); // Remove empty entries

            setItems(newItems);
            toast({
                title: "Item Deleted",
                description: `Deleted ${hsCode} from ${reportingCountry} → ${partnerName} (local update)`,
                variant: "default",
            });
        } finally {
            setDeleteConfirm(null);
        }
    };

    return (
        <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.6 }}
            className="min-h-screen"
            style={{ background: 'transparent' }}
        >
            {/* TOP NAVIGATION */}
            <Header onMenuClick={onMenuClick} showUserInfo={true} />

            {/* MAIN CONTENT */}
            <div className="container mx-auto px-4 py-8 max-w-7xl">
                {/* Page Header */}
                <motion.div
                    initial={{ y: -20, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    transition={{ duration: 0.5 }}
                    className="text-center mb-8"
                >
                    <h1
                        className="text-3xl font-bold mb-2"
                        style={{ color: colors.foreground }}
                    >
                        Business Insights
                    </h1>
                    <p
                        className="text-lg"
                        style={{ color: colors.muted }}
                    >
                        Manage your tariff data and analyze trade patterns
                    </p>
                </motion.div>

                {/* Add Item Form */}
                <motion.div
                    initial={{ y: 20, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    transition={{ duration: 0.5, delay: 0.1 }}
                >
                    <Card
                        className="mb-8 shadow-lg"
                        style={{
                            backgroundColor: colors.surface,
                            borderColor: colors.border
                        }}
                    >
                        <CardHeader>
                            <CardTitle
                                className="flex items-center gap-2"
                                style={{ color: colors.foreground }}
                            >
                                <Plus className="h-5 w-5" />
                                Add Tariff Item
                            </CardTitle>
                            <CardDescription style={{ color: colors.muted }}>
                                Add items to track tariff rates between countries
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
                                            backgroundColor: colors.input,
                                            borderColor: colors.border,
                                            color: colors.foreground,
                                        }}
                                    />
                                </div>
                                <div className="flex items-end">
                                    <Button
                                        onClick={handleAddItem}
                                        className="w-full h-10 transition-all duration-200 hover:scale-[1.02] shadow-md"
                                        style={{
                                            backgroundColor: colors.accent,
                                            borderColor: colors.accent,
                                            color: 'white'
                                        }}
                                        onMouseEnter={(e) => {
                                            e.target.style.backgroundColor = colors.hover || colors.accent;
                                        }}
                                        onMouseLeave={(e) => {
                                            e.target.style.backgroundColor = colors.accent;
                                        }}
                                    >
                                        <Plus className="h-4 w-4 mr-2" />
                                        Add Item
                                    </Button>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </motion.div>

                {/* Items List */}
                {items.length > 0 && (
                    <motion.div
                        initial={{ y: 20, opacity: 0 }}
                        animate={{ y: 0, opacity: 1 }}
                        transition={{ duration: 0.5, delay: 0.2 }}
                    >
                        <Card
                            className="shadow-lg"
                            style={{
                                backgroundColor: colors.surface,
                                borderColor: colors.border
                            }}
                        >
                            <CardHeader>
                                <CardTitle
                                    className="flex items-center gap-2"
                                    style={{ color: colors.foreground }}
                                >
                                    <CalculatorIcon className="h-5 w-5" />
                                    Tariff Items List
                                </CardTitle>
                                <CardDescription style={{ color: colors.muted }}>
                                    List of items with tariff rates based on selected countries.
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div className="overflow-x-auto relative">
                                    <table className="w-full text-sm text-left">
                                        <thead
                                            className="text-xs uppercase"
                                            style={{
                                                backgroundColor: colors.muted,
                                                color: colors.mutedForeground,
                                            }}
                                        >
                                            <tr>
                                                <th scope="col" className="px-6 py-3 rounded-tl-lg">
                                                    Reporting Country (Importer)
                                                </th>
                                                <th scope="col" className="px-6 py-3">
                                                    Partner Country (Exporter)
                                                </th>
                                                <th scope="col" className="px-6 py-3">
                                                    Item/Item Description
                                                </th>
                                                <th scope="col" className="px-6 py-3">
                                                    Tariff Rate
                                                </th>
                                                <th scope="col" className="px-6 py-3 rounded-tr-lg">
                                                    Actions
                                                </th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {items.map((entry, idx) => (
                                                entry.partner.map((partnerCountry, pIdx) => (
                                                    <tr
                                                        key={`${idx}-${pIdx}`}
                                                        className="border-b hover:bg-opacity-50 transition-colors duration-200"
                                                        style={{
                                                            borderColor: colors.border,
                                                            backgroundColor: 'transparent'
                                                        }}
                                                        onMouseEnter={(e) => {
                                                            e.target.closest('tr').style.backgroundColor = colors.hover + '20';
                                                        }}
                                                        onMouseLeave={(e) => {
                                                            e.target.closest('tr').style.backgroundColor = 'transparent';
                                                        }}
                                                    >
                                                        <td
                                                            className="px-6 py-4 font-medium"
                                                            style={{ color: colors.foreground }}
                                                        >
                                                            {entry.report}
                                                        </td>
                                                        <td
                                                            className="px-6 py-4"
                                                            style={{ color: colors.foreground }}
                                                        >
                                                            {partnerCountry}
                                                        </td>
                                                        <td
                                                            className="px-6 py-4"
                                                            style={{ color: colors.foreground }}
                                                        >
                                                            {entry.hsCode[pIdx]}
                                                        </td>
                                                        <td
                                                            className="px-6 py-4 font-semibold"
                                                            style={{ color: colors.accent }}
                                                        >
                                                            {entry.rate[pIdx]}%
                                                        </td>
                                                        <td className="px-6 py-4">
                                                            <Button
                                                                variant="destructive"
                                                                size="sm"
                                                                onClick={() => handleDelete(entry.report, pIdx)}
                                                                className="transition-all duration-300 hover:scale-105 shadow-md"
                                                                style={{
                                                                    backgroundColor: colors.error || '#ef4444',
                                                                    borderColor: colors.error || '#ef4444',
                                                                    color: 'white'
                                                                }}
                                                                onMouseEnter={(e) => {
                                                                    e.target.style.backgroundColor = colors.error ? colors.error + 'cc' : '#dc2626';
                                                                    e.target.style.transform = 'scale(1.05)';
                                                                }}
                                                                onMouseLeave={(e) => {
                                                                    e.target.style.backgroundColor = colors.error || '#ef4444';
                                                                    e.target.style.transform = 'scale(1)';
                                                                }}
                                                            >
                                                                <Trash2 className="h-4 w-4 mr-1" />
                                                                Delete
                                                            </Button>
                                                        </td>
                                                    </tr>
                                                ))
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            </CardContent>
                        </Card>
                    </motion.div>
                )}

                {/* Empty State */}
                {items.length === 0 && (
                    <motion.div
                        initial={{ scale: 0.9, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ duration: 0.5, delay: 0.3 }}
                        className="text-center py-16"
                    >
                        <div
                            className="inline-flex items-center justify-center w-16 h-16 rounded-full mb-4"
                            style={{ backgroundColor: colors.accent + '20' }}
                        >
                            <CalculatorIcon
                                className="h-8 w-8"
                                style={{ color: colors.accent }}
                            />
                        </div>
                        <h3
                            className="text-xl font-semibold mb-2"
                            style={{ color: colors.foreground }}
                        >
                            No Tariff Items Yet
                        </h3>
                        <p
                            className="text-base mb-6 max-w-md mx-auto"
                            style={{ color: colors.muted }}
                        >
                            Start by adding your first tariff item to analyze trade costs between countries.
                        </p>
                    </motion.div>
                )}
            </div>

            {/* Delete Confirmation Dialog */}
            <AnimatePresence>
                {deleteConfirm && (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
                        onClick={() => setDeleteConfirm(null)}
                    >
                        <motion.div
                            initial={{ scale: 0.9, opacity: 0 }}
                            animate={{ scale: 1, opacity: 1 }}
                            exit={{ scale: 0.9, opacity: 0 }}
                            className="max-w-md w-full"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <Card
                                style={{
                                    backgroundColor: colors.surface,
                                    borderColor: colors.border
                                }}
                            >
                                <CardHeader>
                                    <CardTitle
                                        className="flex items-center gap-2"
                                        style={{ color: colors.error || '#ef4444' }}
                                    >
                                        <AlertCircle className="h-5 w-5" />
                                        Confirm Delete
                                    </CardTitle>
                                    <CardDescription style={{ color: colors.muted }}>
                                        Are you sure you want to delete this tariff item? This action cannot be undone.
                                    </CardDescription>
                                </CardHeader>
                                <CardContent className="space-y-4">
                                    <div 
                                        className="p-3 rounded-lg"
                                        style={{ backgroundColor: colors.muted + '20' }}
                                    >
                                        <p className="text-sm" style={{ color: colors.foreground }}>
                                            <strong>Item:</strong> {deleteConfirm.hsCode}
                                        </p>
                                        <p className="text-sm" style={{ color: colors.foreground }}>
                                            <strong>Route:</strong> {deleteConfirm.reportingCountry} → {deleteConfirm.partnerName}
                                        </p>
                                    </div>
                                    <div className="flex gap-3">
                                        <Button
                                            variant="outline"
                                            onClick={() => setDeleteConfirm(null)}
                                            className="flex-1"
                                            style={{
                                                borderColor: colors.border,
                                                color: colors.foreground,
                                                backgroundColor: colors.surface
                                            }}
                                        >
                                            Cancel
                                        </Button>
                                        <Button
                                            onClick={confirmDelete}
                                            className="flex-1"
                                            style={{
                                                backgroundColor: colors.error || '#ef4444',
                                                borderColor: colors.error || '#ef4444',
                                                color: 'white'
                                            }}
                                        >
                                            <Trash2 className="h-4 w-4 mr-2" />
                                            Delete
                                        </Button>
                                    </div>
                                </CardContent>
                            </Card>
                        </motion.div>
                    </motion.div>
                )}
            </AnimatePresence>
        </motion.div>
    );
}