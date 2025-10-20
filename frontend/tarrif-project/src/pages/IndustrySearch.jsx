import { useState, useEffect, useRef } from "react";
import axios from "axios";

export function IndustrySearch() {
  /*
   * Part 1a: Drop down for countries and industry selection
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
  const [destCountry, setDestCountry] = useState("");
  const [homeCountry, setHomeCountry] = useState("");
  const [industry, setIndustry] = useState("");

  const filteredDestCountries = countryList.filter((c) =>
    c.toLowerCase().includes(destCountry.toLowerCase())
  );
  const filteredHomeCountries = countryList.filter((c) =>
    c.toLowerCase().includes(homeCountry.toLowerCase())
  );
  const filteredIndustries = industryList.filter((i) =>
    i.toLowerCase().includes(industry.toLowerCase())
  );

  // states to track which dropdown is open atm
  const [destDropDownOpen, setShowDestDropdown] = useState(false);
  const [homeDropDownOpen, setShowHomeDropdown] = useState(false);
  const [industryDropDownOpen, setIndDropDown] = useState(false);

  // references used to check for user clicking outside the dropdown
  // close dropdown menu if user clicks on something not in the dropdown
  const destRef = useRef(null);
  const homeRef = useRef(null);
  const industryRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (destRef.current && !destRef.current.contains(event.target)) {
        setShowDestDropdown(false);
      }
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
  const handleDestFocus = () => setShowDestDropdown(true);
  const handleHomeFocus = () => setShowHomeDropdown(true);
  const handleIndustryFocus = () => setIndDropDown(true);

  // close the dropdown menu after user makes selection
  const selectDestCountry = (country) => {
    setDestCountry(country);
    setShowDestDropdown(false);
  };

  const selectHomeCountry = (country) => {
    setHomeCountry(country);
    setShowHomeDropdown(false);
  };

  const selectIndustry = (ind) => {
    setIndustry(ind);
    setIndDropDown(false);
  };

  /*
   * Part 1b: Start and End date, input or calendar select
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
  });

  // check the start and end dates
  const validateStartDateChanges = (e) => {
    const newStartDate = e.target.value;
    if (endDate != "" && new Date(newStartDate) > new Date(endDate)) {
      setStartDateError(true);
      console.log("End date cannot be before start date.");
      return;
    }
    setStartDate(newStartDate);
  };

  const validateEndDateChanges = (e) => {
    const newEndDate = e.target.value;
    if (startDate != "" && new Date(newEndDate) < new Date(startDate)) {
      setEndDateError(true);
      console.log("Start date cannot be before end date.");
      return;
    }
    setEndDate(newEndDate);
  };

  return (
    <div>
      <div name="country-industry-filter">
        <h3>Home and Destination Countries</h3>
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

        <div name="dc-select" className="combo-box" ref={destRef}>
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
        </div>

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
                <p color="red">Start date cannot be after end date.</p>
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
                <p color="red">End date cannot be before start date.</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
