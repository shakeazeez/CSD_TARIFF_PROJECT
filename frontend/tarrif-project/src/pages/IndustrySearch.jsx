import { useState, useEffect, useRef } from "react";
import axios from "axios";

export function IndustrySearch() {
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


  return (
    <div>
        <div name="hc-select" className="combo-box" ref={homeRef}>
            <input 
                type="text"
                value={homeCountry}
                onChange={(e) => setHomeCountry(e.target.value)}
                onFocus={handleHomeFocus}
                placeholder="Select home country"/>

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

        <div name ="dc-select" className="combo-box" ref={destRef}>
            <input 
                type="text"
                value={destCountry}
                onChange={(e) => setDestCountry(e.target.value)}
                onFocus={handleDestFocus}
                placeholder="Select destination country"/>

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
    </div>
  );
}
