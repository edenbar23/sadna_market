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

    useEffect(() => {
        const fetchSearchResults = async () => {
            if (!query) return;

            setIsLoading(true);
            setError(null);

            try {
                // Search for products and stores in parallel
                const [productsResults, storesResults] = await Promise.all([
                    searchProducts({ query }),
                    searchStores({ query })
                ]);

                setProducts(productsResults || []);
                setStores(storesResults || []);
            } catch (err) {
                console.error("Search failed:", err);
                setError("Failed to load search results. Please try again.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchSearchResults();
    }, [query]);

    return (
        <div className="container search-results-page">
            <SearchBar initialQuery={query} />

            <h1>Search Results for "{query}"</h1>

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