import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import api from "../lib/api.js";
import IndustryChart from "../components/IndustryChart.jsx";
import { Header } from "../components/Header.jsx";
import Dropdown from "../components/Dropdown.jsx";
import { Label } from "../components/ui/label.jsx";
import { useTheme } from "../contexts/use-theme.js";
import { Input } from "../components/ui/input.jsx"; // input component
import Calendar from "../components/ui/calendar.jsx";
import { AlignCenter } from "lucide-react";
import { useToast } from "../hooks/use-toast.js";

export function Bank({ onMenuClick }) {
    const { colors } = useTheme();

    /*
     * Store information for drop down for countries and industry selection
     */
    const [countryList, setCountryList] = useState([]);
    const [industryList, setIndustryList] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const countryResponse = await api.get(
                    '/tariff/countries'
                );
                setCountryList(countryResponse.data);

                const industryResponse = await api.get(
                    '/tariff/industries'
                ); // endpoint does not exist...yet
                setIndustryList(industryResponse.data);
            } catch (error) {
                console.error("Failed to load data", error);
            }
        };
        fetchData();
    }, []);

    // Transform country list for dropdown component compatibility
    // Converts backend format to {id, code} format expected by Dropdown component
    const modCountryList =
        countryList && Array.isArray(countryList)
            ? countryList.map((item) => ({
                id: item.countryName, // Display name for dropdown
                code: item.countryName, // Value sent to backend
            }))
            : [];

    // Transform industry list for dropdown component compatibility
    const modIndustryList =
        industryList && Array.isArray(industryList)
            ? industryList.map((item) => ({
                id: item, // Display name for dropdown
                code: item, // Value sent to backend
            }))
            : [];

    /* 
     * Take in and store user input for endpoint
     */

    // States to store homecountry and industry selection
    const [homeCountry, setHomeCountry] = useState("");
    const [industry, setIndustry] = useState("");

    // States to store period that user wishes to query 
    const [startDate, setStartDate] = useState(null);
    const [endDate, setEndDate] = useState(null);

    // States to check for valid period selected
    const [startDateError, setStartDateError] = useState(false);
    const [endDateError, setEndDateError] = useState(false);

    // Dates are set to a default range from 1975 to today 
    useEffect(() => {
        const today = new Date();
        const startFrom1975 = new Date(1975, 10, 1); // November 1, 1975

        setStartDate(startFrom1975);
        setEndDate(today);
    }, []);

    // Perform validation on the period selected
    const validateStartDateChanges = (newStartDate) => {
        if (endDate && newStartDate > endDate) {
            setStartDateError(true);
            console.log("End date cannot be before start date.");
            return;
        } else {
            setStartDateError(false);
            setStartDate(newStartDate);
        }
    };

    const validateEndDateChanges = (newEndDate) => {
        if (startDate && newEndDate < startDate) {
            setEndDateError(true);
            console.log("Start date cannot be before end date.");
            return;
        } else {
            setEndDateError(false);
            setEndDate(newEndDate);
        }
    };

    const [itemList, setItemList] = useState([]);
    const [loadingItems, setLoadingItems] = useState(false);
    const [errorItems, setErrorItems] = useState(null);

    // Construct DTO to query backend for the list of all items in the industry
    // Always send 1975-11-01 as start date to get all available data, filter on frontend
    const userInput = {
        homeCountry: homeCountry,
        industry: industry,
        startDate: "1975-11-01",
        endDate: endDate ? endDate.toISOString().split("T")[0] : "",
    };

    const fetchItems = async () => {

        // Set the state to loading of new items for new search criteria
        setLoadingItems(true);

        // REMOVE LATER !!
        setErrorItems(null);

        setItemList([]);
        setSelectedItems([]);

        // Clear all previous results for a fresh search 
        setTariffDetails({});
        setValidItemDetailsMap({});

        // Reset the invalid items from the previous search
        setInvalidItems([]);

        try {
            const response = await api.post(
                '/tariff/items',
                userInput
            );

            console.log("Successfully retrieved items of country: ", homeCountry, ", and industry: ", industry);
            setItemList(response.data);

            if (response.data.length === 0) {
                toast({
                    title: "No items found",
                    description: "No items have tariff data for the selected date range. Showing items available in the last 10 years.",
                });
                // Refetch with default dates
                const defaultUserInput = {
                    homeCountry: homeCountry,
                    industry: industry,
                    startDate: "",
                    endDate: "",
                };
                try {
                    const defaultResponse = await api.post(
                        '/tariff/items',
                        defaultUserInput
                    );
                    setItemList(defaultResponse.data);
                } catch (error) {
                    console.error("Failed to retrieve default items", error);
                }
            }

        } catch {
            console.error("Failed to retrieve items for input DTO ", userInput);
            setErrorItems("Unable to retrieve items.");

        } finally {
            setLoadingItems(false);
        }
    };

    /*
     * Track item options that user selects to load tariff details about
     */

    const backupCountries = useMemo(() => [
        "Cambodia",
        "Chile",
        "Costa Rica",
        "Hong Kong",
        "India",
        "Macao",
        "Malaysia",
        "Indonesia",
        "Myanmar",
        "Australia",
        "Iceland",
        "Georgia"
    ], []);

    // used to keep track of items user selects to load
    const [selectedItems, setSelectedItems] = useState([]);
    const prevSelectedItemsRef = useRef([]);

    // Map to store all the tariff details for existing items that have been queried 
    const [existingItemDetailsMap, setExistingItemDetailsMap] = useState({});

    const { toast } = useToast();

    // Array to store invalid items for displaying error messages 
    const [invalidItems, setInvalidItems] = useState([]);

    // Function to fetch historical tariff for items and backup partner countries
    const fetchPast = useCallback(async (tariffCalculationQueryDTO) => {

        try {
            // POST request to get historical tariff data
            await api.post(
                '/tariff/past',
                tariffCalculationQueryDTO
            );

        } catch (error) {
            console.error("Error fetching historical tariff data:", error);
        }
    }, []);

    // Method to fetch current tariff information for items and backup partner countries 
    const fetchCurrent = useCallback(async (tariffCalculationQueryDTO) => {
        try {
            // POST request to get current tariff calculation
            const response = await api.post(
                '/tariff/current',
                tariffCalculationQueryDTO
            );

            console.log("fetchCurrent response:", response.data);

        } catch (error) {
            console.error("Error fetching current tariff:", error);

        } finally {

            fetchPast(tariffCalculationQueryDTO); // Automatically fetch historical data after current calculation
        }
    }, [fetchPast]);


    const queryTariffs = useCallback((backupCountries, selectedItems) => {
        const filteredItems = selectedItems.filter(item => !invalidItems.includes(item));
        backupCountries.forEach((partnerCountry) => {
            filteredItems.forEach((item) => {
                const tariffCalculationQueryDTO = {
                    reportingCountry: homeCountry,
                    partnerCountry: partnerCountry,
                    item: item,
                    itemCost: 100,
                };

                fetchCurrent(tariffCalculationQueryDTO);
            });
        });
    }, [invalidItems, homeCountry, fetchCurrent]);

    /* 
     * Method to fetch tariff details for each item 1 second after selection change
     */
    const fetchTariffDetails = useCallback(async () => {
        if (selectedItems.length === 0) return;

        setLoadingDetails(true);

        console.log("Before filtering: ", selectedItems);

        // Remove known invalid items from the selected list
        const filteredItems = selectedItems.filter(item => !invalidItems.includes(item));
        console.log("After filtering ", filteredItems);

        for (let i = 0; i < filteredItems.length; i++) {

            const currentItem = filteredItems[i];

            // If the item has been queried before, apply date filter and add it to the valid map to display later
            if (currentItem in existingItemDetailsMap) {
                const originalData = existingItemDetailsMap[currentItem];
                const filteredTariffData = {
                    ...originalData,
                    tariffDetailsList: originalData.tariffDetailsList.map(tariffDetails => ({
                        ...tariffDetails,
                        tariffList: tariffDetails.tariffList.filter(tariff => {
                            if (!tariff.localDate) return false;
                            const tariffDate = new Date(tariff.localDate);
                            const userStartDate = startDate || new Date("1975-11-01");
                            const userEndDate = endDate || new Date();
                            return tariffDate >= userStartDate && tariffDate <= userEndDate;
                        })
                    })).filter(tariffDetails => tariffDetails.tariffList.length > 0)
                };

                setValidItemDetailsMap(prev => ({
                    ...prev,
                    [currentItem]: filteredTariffData
                }));

                setTariffDetails(prev => ({
                    ...prev,
                    [currentItem]: filteredTariffData
                }));
                continue;
            }

            try {
                console.log("Attempting to get tariff details for item ", filteredItems[i]);
                const response = await api.post(
                    '/tariff/items/tariffDetails',
                    {
                        selectedItem: filteredItems[i],
                        homeCountry: homeCountry,
                        industry: industry,
                        startDate: "1975-11-01", // Always send 1975-11-01 to backend
                        endDate: endDate ? endDate.toISOString().split("T")[0] : "",
                    }
                );

                console.log("Successfully retrieved tariff details for item ", filteredItems[i], "Number of countries: ", response.data.tariffDetailsList.length);
                console.log("Tariff Details ", response.data);

                if (!response.data || !response.data.tariffDetailsList || response.data.tariffDetailsList.length == 0) {
                    throw new Error(`No tariff data available for item: ${filteredItems[i]}`);
                }

                const filteredTariffData = {
                    ...response.data,
                    tariffDetailsList: response.data.tariffDetailsList.map(tariffDetails => ({
                        ...tariffDetails,
                        tariffList: tariffDetails.tariffList.filter(tariff => {
                            if (!tariff.localDate) return false;
                            const tariffDate = new Date(tariff.localDate);
                            const userStartDate = startDate || new Date("1975-11-01");
                            const userEndDate = endDate || new Date();
                            return tariffDate >= userStartDate && tariffDate <= userEndDate;
                        })
                    })).filter(tariffDetails => tariffDetails.tariffList.length > 0) // Remove countries with no data in date range
                };

                // Store the item and it's tariff data
                setValidItemDetailsMap(prev => ({
                    ...prev,
                    [currentItem]: filteredTariffData
                }));

                setExistingItemDetailsMap(prev => ({
                    ...prev,
                    [currentItem]: response.data
                }));
                console.log("Item is stored in exisitng map ", existingItemDetailsMap);

                setTariffDetails(prev => ({
                    ...prev,
                    [currentItem]: filteredTariffData
                }));

            } catch (error) {
                setInvalidItems(prev => [...prev, currentItem]);  // add the invalid item into the list 
                console.log("Failed to load tariff for item ", filteredItems[i], error);
            }
        }

        setLoadingDetails(false);

    }, [selectedItems, invalidItems, existingItemDetailsMap, homeCountry, industry, startDate, endDate]);


    useEffect(() => {
        if (Object.keys(existingItemDetailsMap).length === 0) return;

        console.log("Re-filtering with dates:", { startDate, endDate });
        console.log("Existing items:", Object.keys(existingItemDetailsMap));
        console.log("Selected items:", selectedItems);

        const reFilteredTariffDetails = {};

        Object.keys(existingItemDetailsMap).forEach(itemName => {
            const originalData = existingItemDetailsMap[itemName];
            
            console.log(`Processing ${itemName}, original tariff details count:`, originalData.tariffDetailsList?.length);

            const filteredTariffData = {
                ...originalData,
                tariffDetailsList: originalData.tariffDetailsList.map(tariffDetails => ({
                    ...tariffDetails,
                    tariffList: tariffDetails.tariffList.filter(tariff => {
                        if (!tariff.localDate) return false;
                        const tariffDate = new Date(tariff.localDate);
                        const userStartDate = startDate || new Date("1975-11-01");
                        const userEndDate = endDate || new Date();
                        
                        
                        return tariffDate >= userStartDate && tariffDate <= userEndDate;
                    })
                })).filter(tariffDetails => tariffDetails.tariffList.length > 0)
            };
            
            console.log(`${itemName} after filtering: ${filteredTariffData.tariffDetailsList?.length} countries`);

            if (selectedItems.includes(itemName) && filteredTariffData.tariffDetailsList.length > 0) {
                reFilteredTariffDetails[itemName] = filteredTariffData;
                console.log(`Added ${itemName} to results`);
            }
        });

        console.log("Final reFilteredTariffDetails:", Object.keys(reFilteredTariffDetails));
        setTariffDetails(reFilteredTariffDetails);
        setValidItemDetailsMap(reFilteredTariffDetails);

    }, [startDate, endDate, existingItemDetailsMap, selectedItems]);

    useEffect(() => {
        const prevSelectedItems = prevSelectedItemsRef.current;
        const filteredItems = selectedItems.filter(item => !invalidItems.includes(item));
        const newlyAdded = filteredItems.filter(item => !prevSelectedItems.includes(item));

        if (newlyAdded.length > 0) {
            newlyAdded.forEach(item => {
                if (item in existingItemDetailsMap) {
                    // Apply date filter to cached data
                    const originalData = existingItemDetailsMap[item];
                    const filteredTariffData = {
                        ...originalData,
                        tariffDetailsList: originalData.tariffDetailsList.map(tariffDetails => ({
                            ...tariffDetails,
                            tariffList: tariffDetails.tariffList.filter(tariff => {
                                if (!tariff.localDate) return false;
                                const tariffDate = new Date(tariff.localDate);
                                const userStartDate = startDate || new Date("1975-11-01");
                                const userEndDate = endDate || new Date();
                                return tariffDate >= userStartDate && tariffDate <= userEndDate;
                            })
                        })).filter(tariffDetails => tariffDetails.tariffList.length > 0)
                    };

                    setValidItemDetailsMap(prev => ({
                        ...prev,
                        [item]: filteredTariffData
                    }));

                    setTariffDetails(prev => ({
                        ...prev,
                        [item]: filteredTariffData
                    }));

                } else {
                    queryTariffs(backupCountries, newlyAdded);
                }
            });

        }

        prevSelectedItemsRef.current = selectedItems;
    }, [selectedItems, backupCountries, existingItemDetailsMap, invalidItems, queryTariffs]);

    // load and store tariff details for the selected items
    const [tariffDetails, setTariffDetails] = useState({});
    const [loadingDetails, setLoadingDetails] = useState(false);

    // Add a debounce timer ref
    const debounceTimerRef = useRef(null);


    useEffect(() => {
        // Clear the previous timeout if it exists
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }

        // Clear tariff details if no items are selected
        if (selectedItems.length === 0) {
            setTariffDetails({});
            return;
        }

        // Only fetch after 1 second of inactivity
        debounceTimerRef.current = setTimeout(() => {
            fetchTariffDetails();
        }, 1000);

        // Cleanup function to clear timeout when component unmounts
        return () => {
            if (debounceTimerRef.current) {
                clearTimeout(debounceTimerRef.current);
            }
        };
    }, [selectedItems, fetchTariffDetails]);

    useEffect(() => {
        console.log("Item is stored in existing map ", existingItemDetailsMap);
    }, [existingItemDetailsMap]);

    // Map to store all the tariff details for valid items 
    const [validItemDetailsMap, setValidItemDetailsMap] = useState({});

    useEffect(() => {
        console.log("Item is stored in valid map ", validItemDetailsMap);
    }, [validItemDetailsMap]);

    useEffect(() => {
        console.log("INVALID ITEMS ", invalidItems);
    }, [invalidItems]);

    function getTopPartners(tariffDetailsList, n = 3) {
        return [...tariffDetailsList]
            .sort((a, b) => {
                // Sort by average rate since we don't have current rate anymore
                const avgA = a.averageRate;
                const avgB = b.averageRate;
                return avgA - avgB;
            })
            .slice(0, n);
    }

    function getOtherPartners(tariffDetailsList, topPartners) {
        const topCountryNames = topPartners.map((p) => p.country.countryName);
        return tariffDetailsList.filter(
            (p) => !topCountryNames.includes(p.country.countryName)
        );
    }

    function generateYearRange(start, end) {
        // Handle Date objects by converting to string format
        const startStr =
            start instanceof Date ? start.toISOString().split("T")[0] : start;
        const endStr = end instanceof Date ? end.toISOString().split("T")[0] : end;

        const startYear = parseInt(startStr.split("-")[0]);
        const endYear = parseInt(endStr.split("-")[0]);
        const years = [];

        for (let year = startYear; year <= endYear; year++) {
            years.push(year.toString());
        }

        return years;
    }

    // default display top 3, expand to display the list of other tariffs
    const [expandedOther, setExpandedOther] = useState({});

    return (
        <div className="h-screen flex flex-col">
            {/* Header at the top */}
            <Header onMenuClick={onMenuClick} showUserInfo={true} />

            <div className="flex flex-1 min-h-0">
                {/* Left sidebar for search filters */}
                <div
                    className="w-1/4 p-4 border-r overflow-y-auto"
                    style={{
                        backgroundColor: `${colors.surface}15`,
                        borderColor: colors.border,
                    }}
                >
                    <h2
                        className="text-2xl font-bold mb-6 mt-2"
                        style={{ color: colors.foreground }}
                    >
                        ↗ Industry Trends
                    </h2>

                    {/* HERE */}
                    <div
                        className="mb-6 border rounded-md p-4"
                        style={{
                            borderColor: colors.border,
                            backgroundColor: `${colors.surface}95`,
                        }}
                    >
                        <h2
                            className="text-lg font-semibold mb-2"
                            style={{ color: colors.foreground }}
                        >
                            Basic Filter
                        </h2>
                        <p className="text-sm mb-4" style={{ color: colors.muted }}>
                            Filter tariff by reporting country and industry
                        </p>

                        <div className="space-y-2">
                            <Label
                                htmlFor="reporting-country"
                                style={{ color: colors.foreground }}
                            >
                                Reporting Country
                            </Label>
                            <Dropdown
                                options={modCountryList}
                                value={homeCountry}
                                onChange={(option) => {
                                    console.log("Country selected:", option);
                                    setHomeCountry(option ? option.code : "");
                                }}
                                placeholder="Select reporting country"
                                className="w-full"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="industry" style={{ color: colors.foreground }}>
                                Industry
                            </Label>
                            <Dropdown
                                options={modIndustryList}
                                value={industry}
                                onChange={(option) => setIndustry(option ? option.code : "")}
                                placeholder="Select industry"
                                className="w-full"
                            />
                        </div>
                    </div>

                    <div
                        className="mb-6 border rounded-md p-4"
                        style={{
                            borderColor: colors.border,
                            backgroundColor: `${colors.surface}95`,
                        }}
                    >
                        <h2
                            className="text-lg font-semibold mb-2"
                            style={{ color: colors.foreground }}
                        >
                            Date Range Filter
                        </h2>
                        <p className="text-sm mb-4" style={{ color: colors.muted }}>
                            Filter tariff data by date range
                        </p>

                        <div className="mb-3">
                            <Label htmlFor="start-date" style={{ color: colors.foreground }}>
                                From Date
                            </Label>
                            <Calendar
                                placeholder="Select start date"
                                selectedDate={startDate}
                                onDateSelect={validateStartDateChanges}
                                maxDate={endDate}
                            />
                            {startDateError && (
                                <p className="text-xs mt-1" style={{ color: colors.error }}>
                                    Start date cannot be after end date.
                                </p>
                            )}
                        </div>

                        <div>
                            <Label htmlFor="end-date" style={{ color: colors.foreground }}>
                                To Date
                            </Label>
                            <Calendar
                                placeholder="Select end date"
                                selectedDate={endDate}
                                onDateSelect={validateEndDateChanges}
                                minDate={startDate}
                            />
                            {endDateError && (
                                <p className="text-xs mt-1" style={{ color: colors.error }}>
                                    End date cannot be before start date.
                                </p>
                            )}
                        </div>
                    </div>

                    <button
                        onClick={fetchItems}
                        disabled={loadingItems || !homeCountry || !industry}
                        className="w-full py-2 rounded-md flex items-center justify-center mb-6"
                        style={{
                            backgroundColor: colors.accent,
                            alignItems: "center",
                            borderColor: colors.accent,
                            color: "#ffffff",
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.backgroundColor = colors.hover;
                            e.currentTarget.style.color = "#ffffff";
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = colors.accent;
                            e.currentTarget.style.color = "#ffffff";
                        }}
                    >
                        <span className="mr-2"></span>
                        {loadingItems ? "Loading..." : "🔎Search"}
                    </button>

                    <div
                        className="mb-6 border rounded-md p-4"
                        style={{
                            borderColor: colors.border,
                            backgroundColor: `${colors.surface}95`,
                        }}
                    >
                        <h2
                            className="text-lg font-semibold mb-2"
                            style={{ color: colors.foreground }}
                        >
                            Filter Items
                        </h2>
                        <p className="text-sm mb-4" style={{ color: colors.muted }}>
                            {selectedItems.length} of {itemList.length} selected
                        </p>

                        <div className="flex items-center mb-3 text-sm">
                            {/* <button
                onClick={() => {
                  setSelectedItems([...itemList]);

                  const validItems = Object.keys(validItemDetailsMap);
                  const newItems = itemList.filter(item => !validItems.includes(item));

                  setTariffDetails(validItemDetailsMap)

                  if (newItems.length > 0) {
                    console.log("New items to fetch: ", newItems);
                  }
                }}
                className="hover:underline transition-colors duration-200"
                style={{ color: colors.accent }}
                onMouseEnter={(e) => e.target.style.color = colors.hover}
                onMouseLeave={(e) => e.target.style.color = colors.accent}
              >
                All
              </button>*/}
                            {/* <span className="mx-2" style={{ color: colors.muted }}>
                |
              </span>
              <button
                onClick={() => {
                  setSelectedItems([]);
                  setTariffDetails({});
                }}
                className="hover:underline transition-colors duration-200"
                style={{ color: colors.accent }}
                onMouseEnter={(e) => e.target.style.color = colors.hover}
                onMouseLeave={(e) => e.target.style.color = colors.accent}
              >
                None
              </button>*/}
                        </div>

                        {loadingItems && (
                            <p className="text-sm" style={{ color: colors.muted }}>
                                Loading items...
                            </p>
                        )}
                        {errorItems && (
                            <p className="text-sm" style={{ color: colors.error }}>
                                {errorItems}
                            </p>
                        )}

                        <div className="max-h-60 overflow-y-auto space-y-2">
                            {itemList.length > 0 ? (
                                itemList.map((item) => {
                                    const isSelected = selectedItems.includes(item);
                                    return (
                                        <div
                                            key={item}
                                            onClick={() => {
                                                if (isSelected) {
                                                    setSelectedItems(selectedItems.filter(i => i !== item));
                                                } else {
                                                    setSelectedItems([...selectedItems, item]);
                                                }
                                            }}
                                            className="cursor-pointer rounded-md border p-3 transition-all duration-200 hover:shadow-sm"
                                            style={{
                                                borderColor: isSelected ? colors.accent : colors.border,
                                                color: isSelected
                                                    ? colors.foreground
                                                    : colors.foreground,
                                            }}
                                            onMouseEnter={(e) => {
                                                if (!isSelected) {
                                                    e.currentTarget.style.borderColor = colors.accent;
                                                }
                                            }}
                                            onMouseLeave={(e) => {
                                                if (!isSelected) {
                                                    e.currentTarget.style.borderColor = colors.border;
                                                }
                                            }}
                                        >
                                            <div className="flex items-center justify-between">
                                                <span className="text-sm font-medium">{item}</span>
                                                {isSelected && (
                                                    <div
                                                        className="w-4 h-4 rounded-full flex items-center justify-center"
                                                        style={{
                                                            backgroundColor: colors.accent,
                                                        }}
                                                    >
                                                        <span
                                                            className="text-xs"
                                                            style={{ color: colors.background }}
                                                        >
                                                            ✓
                                                        </span>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    );
                                })
                            ) : !loadingItems ? (
                                <div
                                    className="text-center py-8 px-4 rounded-md border-2 border-dashed"
                                    style={{
                                        borderColor: colors.border,
                                        color: colors.muted,
                                    }}
                                >
                                    <p className="text-sm">
                                        No items found. Try different search criteria.
                                    </p>
                                </div>
                            ) : null}
                        </div>
                    </div>
                </div>

                {/* Main content area for results */}
                <div className="w-3/4 p-6 overflow-y-auto">
                    <h2
                        className="text-2xl font-bold mb-6"
                        style={{ color: colors.foreground }}
                    >
                        Results
                    </h2>
                    {/* Loading and error states */}
                    {loadingDetails && (
                        <div
                            className="p-6 rounded-md shadow-sm mb-6"
                            style={{
                                color: colors.foreground,
                            }}
                        >
                            <p style={{ color: colors.muted }}>Loading tariff details...</p>
                        </div>
                    )}

                    {/* Item-specific errors */}
                    {invalidItems.filter(item => selectedItems.includes(item)).length > 0 && (
                        <div
                            className="p-6 rounded-md shadow-sm mb-6 border-l-4"
                            style={{
                                borderColor: colors.warning,
                                backgroundColor: `${colors.warning}10`,
                                color: colors.foreground,
                            }}
                        >
                            <h3
                                className="font-semibold mb-2"
                                style={{ color: colors.warning }}
                            >
                                No tariff information found:
                            </h3>
                            <ul className="list-disc list-inside space-y-1">
                                {invalidItems
                                    .filter(item => selectedItems.includes(item))
                                    .map((error, index) => (
                                        <li key={index} className="test-sm" style={{ color: colors.foreground }}>
                                            {error}
                                        </li>
                                    ))}
                            </ul>
                        </div>
                    )}


                    {Object.values(tariffDetails)
                        .filter((item) => selectedItems.includes(item.itemName))
                        .map((item) => {
                            const tariffDetailsList = item.tariffDetailsList;
                            const topPartners = getTopPartners(tariffDetailsList, 3);
                            const otherPartners = getOtherPartners(
                                tariffDetailsList,
                                topPartners
                            );

                            return (
                                <div
                                    key={item.hscode}
                                    className="mb-8 rounded-md overflow-hidden border"
                                    style={{
                                        borderColor: "colors.border",
                                        backgroundColor: `${colors.surface}95`,
                                    }}
                                >
                                    {/* Item header */}
                                    <div className="border-b border-gray-100 p-6 flex justify-between items-center mb-6">
                                        <div>
                                            <div className="text-gray-500 text-sm">{item.hscode}</div>
                                            <h2 className="text-lg font-medium">{item.itemName}</h2>
                                        </div>
                                    </div>

                                    {/* Top 3 section */}
                                    <div className="p-6 mb-8">
                                        <div className="mb-4">
                                            <h3 className="text-lg font-medium">
                                                Top 3 Best Tariff Rate Partner Countries
                                            </h3>
                                        </div>

                                        <div className="grid grid-cols-3 gap-6">
                                            {topPartners.map((partner) => {
                                                const tariffs = partner.tariffList || [];

                                                const labels =
                                                    tariffs.length > 0
                                                        ? tariffs.map(
                                                            (t) =>
                                                                t.localDate?.split("-")[0] ||
                                                                (startDate instanceof Date ? startDate.getFullYear().toString() : startDate?.split("-")[0])
                                                        )
                                                        : generateYearRange(startDate, endDate);

                                                // Fix chart data formatting
                                                const value =
                                                    tariffs.length > 0
                                                        ? tariffs.map((t) => t.percentageRate.toFixed(2))
                                                        : [0];


                                                console.log("Chart data for", partner.country.countryName, { value, labels })
                                                const avg = partner.averageRate;

                                                return (
                                                    <div
                                                        key={partner.country.countryName}
                                                        className="partner-box border rounded-lg p-6"
                                                        style={{
                                                            borderColor: colors.border,
                                                            backgroundColor: `${colors.surface}98`,
                                                        }}
                                                    >
                                                        <h4 className="text-md font-medium mb-2">
                                                            {partner.country.countryName}
                                                        </h4>
                                                        <div className="h-[300px] mb-6">
                                                            <IndustryChart
                                                                labels={labels}
                                                                value={[value]}
                                                            />
                                                        </div>
                                                        <div className="text-center">
                                                            <span
                                                                className="text-sm font-medium"
                                                                style={{ color: colors.foreground }}
                                                            >
                                                                Average Rate: {avg.toFixed(2)}%
                                                            </span>
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>

                                    {/* Divider */}
                                    <div className="border-t border-gray-300 mx-6 my-10"></div>

                                    {/* Other partner countries section */}
                                    <div className="px-6 pt-4 pb-6">
                                        <div
                                            className="flex items-center justify-between cursor-pointer"
                                            onClick={() =>
                                                setExpandedOther((prev) => ({
                                                    ...prev,
                                                    [item.hscode]: !prev[item.hscode],
                                                }))
                                            }
                                        >
                                            <h3 className="text-lg font-medium">
                                                Other Partner Countries Average Rate
                                            </h3>
                                            <span>{expandedOther[item.hscode] ? "▲" : "▼"}</span>
                                        </div>

                                        {expandedOther[item.hscode] && (
                                            <div className="mt-4">
                                                <div className="space-y-2">
                                                    {otherPartners.map((partner) => {
                                                        const avg = partner.averageRate || 0;
                                                        return (
                                                            <div
                                                                key={partner.country.countryName}
                                                                className="flex justify-between items-center p-3 border rounded-md"
                                                                style={{
                                                                    borderColor: colors.border,
                                                                    backgroundColor: `${colors.surface}98`,
                                                                }}
                                                            >
                                                                <span
                                                                    className="text-sm font-medium"
                                                                    style={{ color: colors.foreground }}
                                                                >
                                                                    {partner.country.countryName}
                                                                </span>
                                                                <span
                                                                    className="text-sm font-medium"
                                                                    style={{ color: colors.foreground }}
                                                                >
                                                                    {avg.toFixed(2)}%
                                                                </span>
                                                            </div>
                                                        );
                                                    })}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}

                    {/* No results state */}
                    {Object.keys(tariffDetails).length === 0 &&
                        !loadingDetails &&
                        selectedItems.length > 0 && (
                            <div
                                className="p-6 rounded-md shadow-sm text-center mt-8"
                                style={{
                                    color: colors.foreground,
                                }}
                            >
                                <p style={{ color: colors.muted }}>
                                    Select items and search to view tariff details
                                </p>
                            </div>
                        )}
                </div>
            </div>
        </div>
    );
}
