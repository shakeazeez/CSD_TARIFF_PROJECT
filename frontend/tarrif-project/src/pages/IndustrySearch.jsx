import { useState, useEffect, useRef } from "react";
import axios from "axios";
import Chart from "../components/Chart";
import { styleEffect } from "framer-motion";

export function IndustrySearch() {
  const backendURL = import.meta.env.VITE_BACKEND_URL;

  /*
   * Drop down for countries and industry selection
   */

  // load the list of countries and industries for dropdown and store in list
  const [countryList, setCountryList] = useState([]);
  const [industryList, setIndustryList] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const countryResponse = await axios.get(`${backendURL}/tariff/countries`);
        setCountryList(countryResponse.data);

        const industryResponse = await axios.get(`${backendURL}/tariff/industries`); // endpoint does not exist...yet
        setIndustryList(industryResponse.data);
      } catch (error) {
        console.error("Failed to load data", error);
      }
    };
    fetchData();
  }, []);

  // states to hold input that user enters
  // const [destCountry, setDestCountry] = useState("");
  const [homeCountry, setHomeCountry] = useState("");
  const [industry, setIndustry] = useState("");

  // Update filtering to handle Country objects correctly
  const filteredHomeCountries = countryList.filter((c) => 
    c.countryName.toLowerCase().includes(homeCountry.toLowerCase())
  );

  const filteredIndustries = industryList.filter((i) =>
    i.toLowerCase().includes(industry.toLowerCase())
  );

  // states to track which dropdown is open atm
  // const [destDropDownOpen, setShowDestDropdown] = useState(false);
  const [homeDropDownOpen, setShowHomeDropdown] = useState(false);
  const [industryDropDownOpen, setIndDropDown] = useState(false);

  // references used to check for user clicking outside the dropdown
  // close dropdown menu if user clicks on something not in the dropdown
  // const destRef = useRef(null);
  const homeRef = useRef(null);
  const industryRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      //   if (destRef.current && !destRef.current.contains(event.target)) {
      //     setShowDestDropdown(false);
      //   }
      if (homeRef.current && !homeRef.current.contains(event.target)) {
        setShowHomeDropdown(false);
      }
      if (industryRef.current && !industryRef.current.contains(event.target)) {
        setIndDropDown(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // focus to show dropdown when selected by user
  // const handleDestFocus = () => setShowDestDropdown(true);
  const handleHomeFocus = () => setShowHomeDropdown(true);
  const handleIndustryFocus = () => setIndDropDown(true);

  // close the dropdown menu after user makes selection
  //   const selectDestCountry = (country) => {
  //     setDestCountry(country);
  //     setShowDestDropdown(false);
  //   };

  // Update to handle Country objects
  const selectHomeCountry = (country) => {
    setHomeCountry(country.countryName);
    setShowHomeDropdown(false);
  };

  const selectIndustry = (ind) => {
    setIndustry(ind);
    setIndDropDown(false);
  };

  /*
   * Start and End date, input or calendar select
   */

  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const [startDateError, setStartDateError] = useState(false);
  const [endDateError, setEndDateError] = useState(false);

  // set default start and end date range (in case users don't enter date range)
  useEffect(() => {
    const today = new Date();
    const tenYearsAgo = new Date();
    tenYearsAgo.setFullYear(today.getFullYear() - 10);

    const formatDate = (date) => date.toISOString().split("T")[0];

    setStartDate(formatDate(tenYearsAgo));
    setEndDate(formatDate(today));
  }, []);

  // check the start and end dates
  const validateStartDateChanges = (e) => {
    const newStartDate = e.target.value;
    if (endDate != "" && new Date(newStartDate) > new Date(endDate)) {
      setStartDateError(true);
      console.log("End date cannot be before start date.");
      return;
    } else {
      setStartDateError(false);
      setStartDate(newStartDate);
    }
  };

  const validateEndDateChanges = (e) => {
    const newEndDate = e.target.value;
    if (startDate != "" && new Date(newEndDate) < new Date(startDate)) {
      setEndDateError(true);
      console.log("Start date cannot be before end date.");
      return;
    } else {
      setEndDateError(false);
      setEndDate(newEndDate);
    }
  };

  /*
   * Send query to backend, get items list
   */

  const [itemList, setItemList] = useState([]);
  const [loadingItems, setLoadingItems] = useState(false);
  const [errorItems, setErrorItems] = useState(null);

  // BACKEND returns => items of the selected industry
  const userInput = {
    homeCountry: homeCountry,
    // destCountry,
    industry: industry,
    startDate: startDate,
    endDate: endDate,
  };

  const fetchItems = async () => {
    setLoadingItems(true);
    try {
      const response = await axios.post(`${backendURL}/tariff/items`, userInput);
      console.log("Retrieving items of homeCountry: industry.");
      // The response contains a list of strings (item names)
      setItemList(response.data);
    } catch (error) {
      console.error("Failed to retrieve items.");
      setErrorItems("Unable to retrieve items.");
    } finally {
      setLoadingItems(false);
    }
  };

  /*
   * Track item options that user selects to load tariff details about
   */

  // used to keep track of items user selects to load
  const [selectedItems, setSelectedItems] = useState([]);

  // load and store tariff details for the selected items
  const [tariffDetails, setTariffDetails] = useState({});
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [errorDetails, setErrorDetails] = useState(null);

  // Add a debounce timer ref
  const debounceTimerRef = useRef(null);

  useEffect(() => {
    // Clear the previous timeout if it exists
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    // Only fetch after 1 second of inactivity
    if (selectedItems.length > 0) {
      debounceTimerRef.current = setTimeout(() => {
        fetchTariffDetails();
      }, 1000);
    }

    // Cleanup function to clear timeout when component unmounts
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, [selectedItems]);

  const fetchTariffDetails = async () => {
    if (selectedItems.length === 0) return;

    setLoadingDetails(true);
    setErrorDetails(null);

    // BACKEND returns => hscode, item name, current tariff, list of partner countries (rates and corresponding dates)
    try {
      // Update the endpoint to match the backend controller mapping
      const response = await axios.post(`${backendURL}/tariff/items/tariffDetails`, {
        selectedItems: selectedItems,
        homeCountry: homeCountry,
        // destCountry,
        industry: industry,
        startDate: startDate,
        endDate: endDate,
      });

      const detailsMap = {};
      response.data.forEach((item) => {
        detailsMap[item.hscode] = item;
      });

      setTariffDetails(detailsMap);
    } catch (error) {
      console.error("Failed to fetch tariff details:", error);
      setErrorDetails("Unable to load tariff details.");
    } finally {
      setLoadingDetails(false);
    }
  };

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
    const startYear = parseInt(start.split("-")[0]);
    const endYear = parseInt(end.split("-")[0]);
    const years = [];
    
    for (let year = startYear; year <= endYear; year++) {
      years.push(year.toString());
    }
    
    return years;
  }

  // default display top 3, expand to display the list of other tariffs
  const [expandedOther, setExpandedOther] = useState({});

  return (
    <div className="flex">
      {/* Left sidebar for search filters */}
      <div className="w-1/4 bg-white p-4 border-r border-gray-200">
        <h2 className="text-2xl font-bold mb-2">Tariff Statistics</h2>

        {/* Basic Filter section with outline */}
        <div className="mb-6 border border-gray-200 rounded-md p-4">
          <h2 className="text-lg font-semibold mb-2">Basic Filter</h2>
          <p className="text-sm text-gray-600 mb-4">
            Filter tariff by reporting country and industry
          </p>

          <div className="mb-3">
            <label className="block text-sm font-medium mb-1">
              Reporting Country
            </label>
            <div className="relative" ref={homeRef}>
              <div className="flex items-center border rounded-md">
                <input
                  type="text"
                  value={homeCountry}
                  onChange={(e) => setHomeCountry(e.target.value)}
                  onFocus={handleHomeFocus}
                  placeholder="Select reporting country"
                  className="w-full p-2 outline-none bg-transparent"
                />
                <span className="px-2 text-gray-400">▼</span>
              </div>
              {homeDropDownOpen && countryList.length > 0 && (
                <ul className="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-auto">
                  {filteredHomeCountries.map((c) => (
                    <li
                      key={c.countryCode || c.countryName}
                      onClick={() => selectHomeCountry(c)}
                      className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                    >
                      {c.countryName}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>

          <div className="mb-3">
            <label className="block text-sm font-medium mb-1">Industry</label>
            <div className="relative" ref={industryRef}>
              <div className="flex items-center border rounded-md">
                <input
                  type="text"
                  value={industry}
                  onChange={(e) => setIndustry(e.target.value)}
                  onFocus={handleIndustryFocus}
                  placeholder="Select industry"
                  className="w-full p-2 outline-none bg-transparent"
                />
                <span className="px-2 text-gray-400">▼</span>
              </div>
              {industryDropDownOpen && industryList.length > 0 && (
                <ul className="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-auto">
                  {filteredIndustries.map((ind) => (
                    <li
                      key={ind}
                      onClick={() => selectIndustry(ind)}
                      className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                    >
                      {ind}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>

        </div>

        <div className="mb-6 border border-gray-200 rounded-md p-4">
          <h2 className="text-lg font-semibold mb-2">Date Range Filter</h2>
          <p className="text-sm text-gray-600 mb-4">
            Filter tariff data by date range
          </p>

          <div className="mb-3">
            <label className="block text-sm mb-1">From Date</label>
            <input
              type="date"
              value={startDate}
              onChange={validateStartDateChanges}
              className="w-full p-2 border rounded-md"
            />
            {startDateError && (
              <p className="text-red-500 text-xs mt-1">
                Start date cannot be after end date.
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm mb-1">To Date</label>
            <input
              type="date"
              value={endDate}
              onChange={validateEndDateChanges}
              className="w-full p-2 border rounded-md"
            />
            {endDateError && (
              <p className="text-red-500 text-xs mt-1">
                End date cannot be before start date.
              </p>
            )}
          </div>
        </div>

        <button
          onClick={fetchItems}
          disabled={loadingItems}
          className="w-full bg-[#B5D1C3] text-gray-700 py-2 rounded-md flex items-center justify-center mb-6"
        >
          <span className="mr-2">🔍</span>
          {loadingItems ? "Loading..." : "Search"}
        </button>
        
        <div className="mb-6 border border-gray-200 rounded-md p-4">
          <h2 className="text-lg font-semibold mb-2">Filter Items</h2>
          <p className="text-sm text-gray-600 mb-4">
            {selectedItems.length} of {itemList.length} selected
          </p>

          <div className="flex items-center mb-3 text-sm">
            <button
              onClick={() => setSelectedItems([...itemList])}
              className="text-blue-600 hover:underline"
            >
              All
            </button>
            <span className="mx-2 text-gray-400">|</span>
            <button
              onClick={() => setSelectedItems([])}
              className="text-blue-600 hover:underline"
            >
              None
            </button>
          </div>

          {loadingItems && (
            <p className="text-sm text-gray-500">Loading items...</p>
          )}
          {errorItems && <p className="text-sm text-red-500">{errorItems}</p>}

          <div className="max-h-60 overflow-y-auto">
            {itemList.length > 0 ? (
              itemList.map((item) => (
                <div key={item} className="flex items-center mb-2">
                  <input
                    type="checkbox"
                    id={item}
                    value={item}
                    checked={selectedItems.includes(item)}
                    onChange={(e) => {
                      const checked = e.target.checked;
                      const value = e.target.value;
                      if (checked) {
                        setSelectedItems([...selectedItems, value]);
                      } else {
                        setSelectedItems(
                          selectedItems.filter((id) => id !== value)
                        );
                      }
                    }}
                    className="mr-2"
                  />
                  <label htmlFor={item} className="text-sm cursor-pointer">
                    {item}
                  </label>
                </div>
              ))
            ) : !loadingItems ? (
              <p className="text-sm text-gray-500">
                No items found. Try different search criteria.
              </p>
            ) : null}
          </div>
        </div>
      </div>

      {/* Main content area for results */}
      <div className="w-3/4 p-6">
        {/* Loading and error states */}
        {loadingDetails && (
          <div className="border border-gray-200 p-4 rounded-md shadow-sm mb-4">
            <p className="text-gray-500">Loading tariff details...</p>
          </div>
        )}

        {errorDetails && (
          <div className="border border-gray-200 p-4 rounded-md shadow-sm mb-4 border-l-4 border-red-500">
            <p className="text-red-500">{errorDetails}</p>
          </div>
        )}

        {Object.values(tariffDetails).map((item) => {
          const tariffDetailsList = item.tariffDetailsList;
          const topPartners = getTopPartners(tariffDetailsList, 3);
          const otherPartners = getOtherPartners(
            tariffDetailsList,
            topPartners
          );

          return (
            <div
              key={item.hscode}
              className="border border-gray-200 mb-8 rounded-md shadow-sm overflow-hidden"
            >
              {/* Item header */}
              <div className="border-b border-gray-100 p-4 flex justify-between items-center">
                <div>
                  <div className="text-gray-500 text-sm">{item.hscode}</div>
                  <h2 className="text-lg font-medium">{item.itemName}</h2>
                </div>
                <button className="text-gray-400 hover:text-gray-600">
                  <span>📌</span>
                </button>
              </div>

              {/* Top 3 section */}
              <div className="p-4">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-medium">
                    Top 3 Best Tariff Rate Partner Countries
                  </h3>
                  <button className="text-gray-400 hover:text-gray-600">
                    <span>▲</span>
                  </button>
                </div>

                <div className="grid grid-cols-3 gap-4">
                  {topPartners.map((partner) => {
                    const tariffs = partner.tariffs || [];

                    const labels =
                      tariffs.length > 0
                        ? tariffs.map(
                            (t) =>
                              t.localDate?.split("-")[0] ||
                              startDate.split("-")[0]
                          )
                        : generateYearRange(startDate, endDate);

                    // Fix chart data formatting
                    const values =
                      tariffs.length > 0
                        ? tariffs.map((t) => t.percentageRate * 100)
                        : [0];

                    const legend = [partner.country.countryName];
                    const avg = partner.averageRate;

                    return (
                      <div
                        key={partner.country.countryName}
                        className="partner-box border rounded-lg p-4"
                      >
                        <h4 className="text-md font-medium mb-2">
                          {partner.country.countryName}
                        </h4>
                        <div className="h-40">
                          <Chart
                            labels={labels}
                            value={values}
                            title="Tariff Over Time"
                            legend={legend}
                          />
                        </div>
                        <div className="mt-2">
                          <div className="text-sm text-gray-500">
                            Current Rate
                          </div>
                          <div className="font-medium">{avg.toFixed(2)}%</div>
                        </div>
                        <div className="mt-2">
                          <div className="text-sm text-gray-500">
                            Avg Past Rate
                          </div>
                          <div className="font-medium">
                            {(avg + 0.7).toFixed(2)}%
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Other partner countries section */}
              <div className="border-t border-gray-100 p-4">
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
                    Other Partner Countries
                  </h3>
                  <span>{expandedOther[item.hscode] ? "▲" : "▼"}</span>
                </div>

                {expandedOther[item.hscode] && (
                  <div className="mt-4">
                    <table className="w-full">
                      <tbody>
                        {otherPartners.map((partner) => {
                          const avg = partner.averageRate || 0;
                          const pastAvg = avg + 0.5 || 0;

                          // Calculate rate change indicator
                          let rateChangeIndicator = null;
                          const rateDiff = (avg - pastAvg).toFixed(2);

                          if (rateDiff < -0.5) {
                            rateChangeIndicator = (
                              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-green-100 text-green-800">
                                ↓ {Math.abs(rateDiff)}% better
                              </span>
                            );
                          } else if (rateDiff > 0.5) {
                            rateChangeIndicator = (
                              <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-red-100 text-red-800">
                                ↑ {Math.abs(rateDiff)}% worse
                              </span>
                            );
                          } else {
                            rateChangeIndicator = (
                              <span className="inline-flex items-center px-2 py-1 text-xs text-gray-500">
                                — No change
                              </span>
                            );
                          }

                          return (
                            <tr
                              key={partner.country.countryName}
                              className="border-b border-gray-100"
                            >
                              <td className="py-3 pl-2 w-1/4">
                                {partner.country.countryName}
                              </td>
                              <td className="py-3">
                                <div className="mb-1">
                                  <span className="text-sm text-gray-500">
                                    Current Rate
                                  </span>
                                  <span className="float-right">
                                    {avg.toFixed(2)}%
                                  </span>
                                </div>
                                <div>
                                  <span className="text-sm text-gray-500">
                                    Avg Past Rate
                                  </span>
                                  <span className="float-right">
                                    {pastAvg.toFixed(2)}%
                                  </span>
                                </div>
                              </td>
                              <td className="py-3 text-right pr-2">
                                {rateChangeIndicator}
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
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
            <div className="border border-gray-200 p-6 rounded-md shadow-sm text-center">
              <p className="text-gray-500">
                Select items and search to view tariff details
              </p>
            </div>
          )}
      </div>
    </div>
  );
}
