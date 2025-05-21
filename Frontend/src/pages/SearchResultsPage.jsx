import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import StoreCard from "../components/StoreCard";
import SearchBar from "../components/SearchBar";
import { searchProducts } from "../api/product";
import { searchStores } from "../api/store";
import "../index.css";

export default function SearchResultsPage() {
    const location = useLocation();
    const query = new URLSearchParams(location.search).get("q") || "";

    const [products, setProducts] = useState([]);
    const [stores, setStores] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    // Prepare search filters
    const [filters, setFilters] = useState({
        minPrice: "",
        maxPrice: "",
        category: "",
        minRating: "",
        maxRating: ""
    });

    // Handle filter changes
    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // Apply filters
    const applyFilters = () => {
        fetchSearchResults(query, filters);
    };

    // Reset filters
    const resetFilters = () => {
        setFilters({
            minPrice: "",
            maxPrice: "",
            category: "",
            minRating: "",
            maxRating: ""
        });
        fetchSearchResults(query, {});
    };

    const fetchSearchResults = async (searchQuery, searchFilters) => {
        if (!searchQuery) return;

        setIsLoading(true);
        setError(null);

        try {
            // Prepare search parameters
            const productParams = {
                name: searchQuery,
                category: searchFilters.category || null,
                minPrice: searchFilters.minPrice ? parseFloat(searchFilters.minPrice) : null,
                maxPrice: searchFilters.maxPrice ? parseFloat(searchFilters.maxPrice) : null,
                minRank: searchFilters.minRating ? parseFloat(searchFilters.minRating) : null,
                maxRank: searchFilters.maxRating ? parseFloat(searchFilters.maxRating) : null
            };

            // Search for products and stores in parallel
            const [productsResponse, storesResponse] = await Promise.all([
                searchProducts(productParams),
                searchStores({ query: searchQuery })
            ]);

            // Process product results
            if (productsResponse && productsResponse.data) {
                setProducts(productsResponse.data);
            } else {
                setProducts([]);
            }

            // Process store results
            if (storesResponse && storesResponse.data) {
                setStores(storesResponse.data);
            } else {
                setStores([]);
            }
        } catch (err) {
            console.error("Search failed:", err);
            setError("Failed to load search results. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchSearchResults(query, filters);
    }, [query]);

    return (
        <div className="container search-results-page">
            <SearchBar initialQuery={query} />

            <h1>Search Results for "{query}"</h1>

            {/* Filter panel */}
            <div className="filter-panel">
                <h3>Filters</h3>
                <div className="filter-grid">
                    <div className="filter-item">
                        <label htmlFor="category">Category</label>
                        <input
                            type="text"
                            id="category"
                            name="category"
                            value={filters.category}
                            onChange={handleFilterChange}
                            placeholder="Enter category"
                        />
                    </div>
                    <div className="filter-item">
                        <label htmlFor="minPrice">Min Price</label>
                        <input
                            type="number"
                            id="minPrice"
                            name="minPrice"
                            value={filters.minPrice}
                            onChange={handleFilterChange}
                            placeholder="Min"
                            min="0"
                        />
                    </div>
                    <div className="filter-item">
                        <label htmlFor="maxPrice">Max Price</label>
                        <input
                            type="number"
                            id="maxPrice"
                            name="maxPrice"
                            value={filters.maxPrice}
                            onChange={handleFilterChange}
                            placeholder="Max"
                            min="0"
                        />
                    </div>
                    <div className="filter-item">
                        <label htmlFor="minRating">Min Rating</label>
                        <input
                            type="number"
                            id="minRating"
                            name="minRating"
                            value={filters.minRating}
                            onChange={handleFilterChange}
                            placeholder="Min"
                            min="0"
                            max="5"
                            step="0.1"
                        />
                    </div>
                    <div className="filter-item">
                        <label htmlFor="maxRating">Max Rating</label>
                        <input
                            type="number"
                            id="maxRating"
                            name="maxRating"
                            value={filters.maxRating}
                            onChange={handleFilterChange}
                            placeholder="Max"
                            min="0"
                            max="5"
                            step="0.1"
                        />
                    </div>
                </div>
                <div className="filter-buttons">
                    <button onClick={applyFilters} disabled={isLoading}>Apply Filters</button>
                    <button onClick={resetFilters} disabled={isLoading}>Reset</button>
                </div>
            </div>

            {isLoading && <div className="loading-indicator">Searching...</div>}

            {error && <div className="error-message">{error}</div>}

            {!isLoading && !error && (
                <>
                    {stores.length > 0 && (
                        <div className="results-section">
                            <h2>Stores ({stores.length})</h2>
                            <div className="store-results">
                                {stores.map((store) => (
                                    <StoreCard key={store.storeId} store={store} />
                                ))}
                            </div>
                        </div>
                    )}

                    {products.length > 0 && (
                        <div className="results-section">
                            <h2>Products ({products.length})</h2>
                            <div className="product-results">
                                {products.map((product) => (
                                    <ProductCard key={product.productId} product={product} />
                                ))}
                            </div>
                        </div>
                    )}

                    {products.length === 0 && stores.length === 0 && !isLoading && (
                        <div className="no-results">
                            <p>No results found for "{query}".</p>
                            <p>Try checking your spelling or using more general keywords.</p>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}