import { useState, useEffect, useRef } from "react";
import axios from "axios";
import Chart from "../components/Chart";

export function IndustrySearch() {
  /*
   * Drop down for countries and industry selection
   */

  // load the list of countries and industries for dropdown and store in list
  const [countryList, setCountryList] = useState([]);
  const [industryList, setIndustryList] = useState([]);
  useEffect(() => {
    const fetchData = async () => {
      try {
        const countryResponse = await axios.get("/tariff/countries");
        setCountryList(countryResponse.data);

        const industryResponse = await axios.get("/tariff/industries"); // endpoint does not exist...yet
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

  //   const filteredDestCountries = countryList.filter((c) =>
  //     c.toLowerCase().includes(destCountry.toLowerCase())
  //   );
  const filteredHomeCountries = countryList.filter((c) =>
    c.toLowerCase().includes(homeCountry.toLowerCase())
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

  const selectHomeCountry = (country) => {
    setHomeCountry(country);
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
    const sevenDaysBefore = new Date();
    sevenDaysBefore.setDate(today.getDate() - 7);

    const formatDate = (date) => date.toISOString().split("T")[0];

    setStartDate(formatDate(sevenDaysBefore));
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
    homeCountry,
    // destCountry,
    industry,
    startDate,
    endDate,
  };

  const fetchItems = async () => {
    setLoadingItems(true);
    try {
      const response = await axios.post("/tariff/items", userInput);
      console.log("Retrieving items of industry.");

      const minimalItems = response.data.map((item) => ({
        ...item,
        detailsLoaded: false,
        expanded: false,
      }));

      setItemList(minimalItems);
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

  useEffect(() => {
    fetchTariffDetails();
  }, [selectedItems]);

  const fetchTariffDetails = async () => {
    if (selectedItems.length === 0) return;

    setLoadingDetails(true);
    setErrorDetails(null);

    // BACKEND returns => hscode, item name, current tariff, list of partner countries (rates and corresponding dates)
    try {
      const response = await axios.post("/tariff/details", {
        items: selectedItems,
        homeCountry,
        industry,
        startDate,
        endDate,
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

  function getTopPartners(partners, n = 3) {
    return [...partners]
      .sort((a, b) => {
        const avgA = a.rates.reduce((sum, r) => sum + r, 0) / a.rates.length;
        const avgB = b.rates.reduce((sum, r) => sum + r, 0) / b.rates.length;
        return avgA - avgB;
      })
      .slice(0, n);
  }

  function getOtherPartners(partners, topPartners) {
    const topCountries = topPartners.map((p) => p.country);
    return partners.filter((p) => !topCountries.includes(p.country));
  }

  // default display top 3, expand to display the list of other tariffs
  const [expandedOther, setExpandedOther] = useState({});

  return (
    <div>
      <div>
        <div name="search-filter">
          <div name="country-industry-filter">
            <h3>Home Country</h3>
            <div name="hc-select" className="combo-box" ref={homeRef}>
              <input
                type="text"
                value={homeCountry}
                onChange={(e) => setHomeCountry(e.target.value)}
                onFocus={handleHomeFocus}
                placeholder="Select home country"
              />

              {homeDropDownOpen && countryList.length > 0 && (
                <ul>
                  {filteredHomeCountries.map((c) => (
                    <li key={c} onClick={() => selectHomeCountry(c)}>
                      {c}
                    </li>
                  ))}
                </ul>
              )}
            </div>

            {/* <div name="dc-select" className="combo-box" ref={destRef}>
            <input
              type="text"
              value={destCountry}
              onChange={(e) => setDestCountry(e.target.value)}
              onFocus={handleDestFocus}
              placeholder="Select destination country"
            />

            {destDropDownOpen && countryList.length > 0 && (
              <ul>
                {filteredDestCountries.map((c) => (
                  <li key={c} onClick={() => selectDestCountry(c)}>
                    {c}
                  </li>
                ))}
              </ul>
            )}
          </div> */}

            <h3>Industry</h3>
            <div name="ind-select" className="combo-box" ref={industryRef}>
              <input
                type="text"
                value={industry}
                onChange={(e) => setIndustry(e.target.value)}
                onFocus={handleIndustryFocus}
                placeholder="Select industry"
              />

              {industryDropDownOpen && industryList.length > 0 && (
                <ul>
                  {industryList.map((id) => (
                    <li key={id} onClick={() => selectIndustry(id)}>
                      {id}
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div name="date-range-filter">
              <h3>Date Range Filter</h3>
              <p>Filter tariff data by date range</p>

              <div style={{ display: "flex", gap: "8px" }}>
                <div name="start-date-picker" className="date-combobox">
                  <input
                    type="date"
                    value={startDate}
                    onChange={validateStartDateChanges}
                    placeholder="dd/mm/yyyy"
                  ></input>

                  {startDateError && (
                    <p style={{ color: "red" }}>
                      Start date cannot be after end date.
                    </p>
                  )}
                </div>

                <div name="end-date-picker" className="date-combobox">
                  <input
                    type="date"
                    value={endDate}
                    onChange={validateEndDateChanges}
                    placeholder="dd/mm/yyyy"
                  ></input>

                  {endDateError && (
                    <p style={{ color: "red" }}>
                      End date cannot be before start date.
                    </p>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div>
            <button
              name="search-button"
              onClick={fetchItems}
              disabled={loadingItems}
            >
              {loadingItems ? "Loading..." : "Search"}
            </button>
          </div>

          <div name="item-multiselect">
            <h3>Select Items to View Tariffs</h3>
            {itemList.map((item) => (
              <div key={item.hscode}>
                <label>
                  <input
                    type="checkbox"
                    value={item.hscode}
                    checked={selectedItems.includes(item.hscode)}
                    onChange={(e) => {
                      const checked = e.target.checked;
                      if (checked) {
                        setSelectedItems([...selectedItems, item.hscode]);
                      } else {
                        setSelectedItems(
                          selectedItems.filter((id) => id !== item.hscode)
                        );
                      }
                    }}
                  />
                  {item.hscode} - {item.name}
                </label>
              </div>
            ))}
          </div>
        </div>
      </div>

      {Object.values(tariffDetails).map((item) => {
        const partners = item.partners; // list of countries returned by backend

        // call helper function to filter and find the top 3 countries with lowest tariff rates 
        const top3 = getTopPartners(partners, 3); 

        // call helper function to filter and return all other countries
        const otherPartners = getOtherPartners(partners, top3);

        return (
          <div
            key={item.hscode}
            className="item-divider"
            style={{ marginBottom: "32px" }}
          >
            <h2>
              {item.hscode} - {item.name}
            </h2>

            {/* Row of 3 best tariffs */}
            <h2>Top 3 Best Tariff Partner Countries</h2>
            <div style={{ display: "flex", gap: "16px" }}>
              {top3.map((partner) => {
                const labels = partner.dates;
                const values = [partner.rates];
                const legend = [partner.country];
                const avg =
                  partner.rates.reduce((sum, r) => sum + r, 0) /
                  partner.rates.length;

                return (
                  <div
                    key={partner.country}
                    className="partner-box"
                    style={{
                      flex: 1,
                      textAlign: "left",
                      border: "1px solid #ccc",
                      padding: "16px",
                      borderRadius: "8px",
                    }}
                  >
                    <h3>{partner.country}</h3>
                    <Chart
                      labels={labels}
                      value={values}
                      title="Tariff Over Time"
                      legend={legend}
                    />
                    <h3>Current Tariff</h3>
                    <p>{partner.current}%</p>
                    <h3>Average Past Tariff</h3>
                    <p>{avg.toFixed(2)}%</p>
                  </div>
                );
              })}
            </div>

            {/* Horizontal divider to split the top 3 and other countries */}
            <hr style={{ margin: "24px 0" }} />

            {/* Other partners */}
            <h2
              style={{ cursor: "pointer" }}
              onClick={() =>
                setExpandedOther((prev) => ({
                  ...prev,
                  [item.hscode]: !prev[item.hscode],
                }))
              }
            >
              Other Partner Countries {expandedOther[item.hscode] ? "▲" : "▼"}
            </h2>

            {expandedOther[item.hscode] && (
              <table
                style={{
                  width: "100%",
                  borderCollapse: "collapse",
                  marginTop: "16px",
                }}
              >
                <thead>
                  <tr
                    style={{
                      textAlign: "left",
                      borderBottom: "2px solid #ccc",
                    }}
                  >
                    <th style={{ padding: "8px" }}>Country</th>
                    <th style={{ padding: "8px" }}>Current Rate</th>
                    <th style={{ padding: "8px" }}>Average Rate</th>
                    <th style={{ padding: "8px" }}>Difference</th>
                  </tr>
                </thead>
                <tbody>
                  {otherPartners.map((partner) => {
                    const avg =
                      partner.rates.reduce((sum, r) => sum + r, 0) /
                      partner.rates.length;
                    const diff = partner.current - avg;
                    const diffText = `${Math.abs(diff).toFixed(2)}% ${
                      diff > 0 ? "worse ▼" : "better ▲"
                    }`;
                    const diffColor = diff > 0 ? "red" : "green";

                    return (
                      <tr
                        key={partner.country}
                        style={{ borderBottom: "1px solid #eee" }}
                      >
                        <td style={{ padding: "8px" }}>{partner.country}</td>
                        <td style={{ padding: "8px" }}>{partner.current}%</td>
                        <td style={{ padding: "8px" }}>{avg.toFixed(2)}%</td>
                        <td style={{ padding: "8px", color: diffColor }}>
                          {diffText}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        );
      })}
    </div>
  );
}
