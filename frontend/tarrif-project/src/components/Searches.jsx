import { useEffect, useState, useCallback } from "react";
import api from "../lib/api.js";

import { useAuth } from '../contexts/use-auth.js'

const Searches = () => {
  const { isAuthenticated } = useAuth()

  // tariffIds are stored in local storage, data are fetched from backend for each tariffIds
  const [recentSearchesIds, setRecentSearchesIds] = useState([]); // searched tariffIds for guest users, max 5
  const [topSearchesIds, setTopSearchesIds] = useState([]); // searched tariffIds for logged in users, max 5

  const [recentSearchesData, setRecentSearchesData] = useState({}); // search details for guest users, [tariffId] -> GeneralTariffDTO
  const [topSearchesData, setTopSearchesData] = useState({}); // search details for logged in users, [tariffId] -> GeneralTariffDTO

  /*
   * For both guest and logged in users,
   * fetch the full search details for a specific tariffId from the backend,
   * and update the recentSearchesData or topSearchesData state accordingly
   * to show the search details on the card on Dashboard & Calculator pages
   */
  const fetchSearches = useCallback(async (tariffId, setDataFunction) => {
    try {
      const response = await api.post(`/tariff/current/${tariffId}`);

      // map tariffId -> response.data
      setDataFunction(prev => ({
        ...prev,
        [tariffId]: response.data,
      }));
    } catch (error) {
      console.error("Error fetching relevant tariff data:", error);
    }
  }, []);

  /* 
   * Fetch top searches ids for logged in users from backend
   * Backend returns List<Integer> of tariffIds which is sorted by search frequency in descending order
   * TariffIds are then stored in local storage and in topSearchesIds state
   * 
   */
  const fetchTopSearchesIds = useCallback(async () => {
    // console.log("Starting fetchTopSearchesIds for logged in user");
    try { // calls backend to get the top searches for logged in users
      const response = await api.get(`/user/${sessionStorage.getItem("username")}/history`); // returns Map<Integer, LocalDate> of tariffIds

      const tariffIds = Object.keys(response.data).map(Number); // array of tariffIds as numbers
      sessionStorage.setItem("generalUserTopSearches", JSON.stringify(tariffIds)); // saves the tariffIds in local storage
      setTopSearchesIds(tariffIds); // update state the fetching of details can be triggered next
    } catch (e) {
      console.error("Error fetching relevant searches:", e);
    }
  }, []);

  /*
   * Loads guest user's recent search history from localStorage.
   * Updates recentSearchesIds state.
   * For every stored tariffId, triggers a fetch to backend to get the full search details. (using fetchSearches function)
   */
  const loadRecentSearches = useCallback(() => {
    // console.log("Loading recent searches for guest user");
    const storedSearchesIds = localStorage.getItem("guestRecentSearches");
    // console.log("Stored guest searches:", storedSearchesIds);

    if (storedSearchesIds) {
      let idsArray;
      try {
        const parsedSearchesIds = JSON.parse(storedSearchesIds);
        if (Array.isArray(parsedSearchesIds)) {
          idsArray = parsedSearchesIds.map(s => Number(s));
        } // else {
        //   idsArray = storedSearchesIds.split(",").map((s) => Number(s.trim()));
        // }
      } catch (error) {
        console.error("Error parsing searches from local storage:", error);
        idsArray = storedSearchesIds.split(",").map((s) => Number(s.trim()));
      }

      // console.log("Guest search IDs array:", idsArray);
      setRecentSearchesIds(idsArray);  // store the tariffIds in state
      idsArray.forEach(tariffId => fetchSearches(tariffId, setRecentSearchesData)); // fetch the search details for each tariffId and store in state
    }
  }, [fetchSearches]);

  /*
   * On component mount:
   * If user is logged in, load top searches from backend
   * If user is guest, load recent searches from local storage
   */
  useEffect(() => {
    // console.log("Searches useEffect triggered, isAuthenticated:", isAuthenticated);
    if (isAuthenticated) {
      fetchTopSearchesIds();
    } else {
      loadRecentSearches();
    }
  }, [isAuthenticated, fetchTopSearchesIds, loadRecentSearches]);

  /*
   * For logged in users:
   * Fetch search details when topSearchesIds changes (after fetchTopSearchesIds completes)
   * Search details are stored in topSearchesData 
   */
  useEffect(() => {
    if (isAuthenticated && topSearchesIds.length > 0) {
      topSearchesIds.forEach(tariffId => fetchSearches(tariffId, setTopSearchesData));
    }
  }, [topSearchesIds, isAuthenticated, fetchSearches]);

  /*
   * After a search is made by guest user,
   * update the recentSearchesIds state and local storage for "guestRecentSearches"
   * and also fetch the search details from backend and update recentSearchesData state
   * 
   * Removes the tariffId from the list if it already exists to avoid duplicates,
   * and adds the new tariffId to the front of the list.
   * Limits the list to the most recent 5 searches.
   */
  const addRecentSearch = (tariffId) => {
    setRecentSearchesIds(prev => {
      const filteredIds = prev.filter((id) => id !== tariffId); // remove the tariffId from the array if it already exists
      let updatedIds = [tariffId, ...filteredIds]; // add the new tariffId to the front
      if (updatedIds.length > 5) {
        updatedIds = updatedIds.slice(0, 5); // remove the last element if the length exceeds 5
      }
      localStorage.setItem("guestRecentSearches", JSON.stringify(updatedIds)); // update local storage
      return updatedIds;
    });

    // update recentSearches state
    if (!recentSearchesData[tariffId]) { // only fetch if the tariffId is not already in the recentSearches state
      fetchSearches(tariffId, setRecentSearchesData); // fetch the search details and update the recentSearches state
    }
  };

  /* 
   * After a search is made by logged in user,
   * update the topSearchesIds state and local storage for "generalUserTopSearches" (fetched from backend)
   * and also fetch the search details from backend and update topSearchesData state
   */
  const addRelevantSearch = async (tariffId) => {
    try {
      const response = await api.post(`/user/${sessionStorage.getItem("username")}/history/${tariffId}`, "");
      const tariffIds = Object.keys(response.data).map(Number); // array of tariffIds as numbers
      sessionStorage.setItem("generalUserTopSearches", JSON.stringify(tariffIds));
      setTopSearchesIds(tariffIds);
      fetchSearches(tariffId, setTopSearchesData);
    } catch (error) {
      console.error("Error adding recent searches:", error);
    }
  }

  // expose the appropriate methods and data for Calculator and Dashboard pages to use (for guest user or general user)
  const addSearch = isAuthenticated ? addRelevantSearch : addRecentSearch; // backend for logged in, localStorage for guest
  const ids = Array.isArray(isAuthenticated ? topSearchesIds : recentSearchesIds) ? (isAuthenticated ? topSearchesIds : recentSearchesIds) : []; // tariffIds
  const data = isAuthenticated ? topSearchesData : recentSearchesData; // search details of each tariffId

  return {
    addSearch, // function to add a search (either recent or top searches based on authentication)
    searchesData: data, // search data mapping tariffId -> GeneralTariffDTO
    searchesIds: ids, // array of tariffIds
    SearchDisplay: ({ onSearchClick, colors }) => (
      <div>
        {(!ids || !Array.isArray(ids) || ids.length === 0) ? (
          <div className="text-center py-6" style={{ color: colors?.muted || '#6b7280' }}>
            <p>{isAuthenticated ? "No favourite searches yet" : "No recent searches"}</p>
          </div>
        ) : (
          <div className="flex flex-wrap gap-3 justify-center">
            {ids.map((id) => {
              const searchData = data[id];

              if (!searchData) return null;

              return (
                <div
                  key={id}
                      className="flex-shrink-0 p-3 rounded-lg cursor-pointer transition-all duration-200 shadow hover:shadow-lg flex items-center justify-center w-full sm:w-44"
                  style={{
                    backgroundColor: `${colors?.border || '#e5e7eb'}80`,
                    borderColor: `${colors?.border || '#e5e7eb'}80`,
                    borderWidth: '1px',
                    minHeight: '120px',
                  }}
                  onClick={() => onSearchClick && onSearchClick(searchData)}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = colors?.accent || '#3b82f6';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = colors?.border || '#e5e7eb';
                  }}
                >
                  <div className="space-y-2 text-center w-full">
                    <div className="text-sm font-medium" style={{ color: colors?.foreground || '#111827' }}>
                      {searchData.partnerCountry} → {searchData.reportingCountry}
                    </div>
                    <div className="text-xs" style={{ color: colors?.muted || '#6b7280' }}>
                      {searchData.item}
                    </div>
                    <div className="text-xl font-bold" style={{ color: colors?.accent || '#3b82f6' }}>
                      {(searchData.tariff).toFixed(2)}%
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    ),
  };
}

export default Searches;