// components/SearchBar/SearchBar.js
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import FiltersModal from "./FiltersModal";
import { searchProducts } from "../api/product";
import "../styles/components.css";
import ErrorAlert from "./ErrorAlert";

export default function SearchBar({ storeId }) {
  const [query, setQuery] = useState("");
  const [filters, setFilters] = useState({
    category: "",
    minPrice: "",
    maxPrice: "",
    minRank: "",
    maxRank: "",
  });
  const [showFilters, setShowFilters] = useState(false);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const handleSearch = async () => {
    const searchRequest = {
      name: query.trim(),
      category: filters.category || undefined,
      minPrice: filters.minPrice ? parseFloat(filters.minPrice) : undefined,
      maxPrice: filters.maxPrice ? parseFloat(filters.maxPrice) : undefined,
      minRank: filters.minRank ? parseFloat(filters.minRank) : undefined,
      maxRank: filters.maxRank ? parseFloat(filters.maxRank) : undefined,
      storeId: storeId || undefined
    };

    try {
      const response = await searchProducts(searchRequest);
      if (response.error || (response.data && response.data.error)) {
        setError(response.error || response.data.error);
        return;
      }
      navigate(`/search`, { state: { results: response.data } });
    } catch (error) {
      setError("Failed to fetch search results. Check the console.");
    }
  };

  const updateFilter = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  return (
      <div className="search-bar-container">
        {error && <ErrorAlert message={error} onClose={() => setError("")} />}
        <input
            type="text"
            placeholder={storeId ? "Search products in this store..." : "Search products..."}
            className="search-input"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSearch()}
        />
        <button className="button" onClick={handleSearch}>Search</button>
        <button className="button" onClick={() => setShowFilters(true)}>Filters</button>

        {showFilters && (
            <FiltersModal
                filters={filters}
                onChange={updateFilter}
                onClose={() => setShowFilters(false)}
            />
        )}
      </div>
  );
}