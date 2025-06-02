// pages/SearchResultsPage.js
import React from "react";
import { useLocation } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import "../styles/SearchResultsPage.css";

export default function SearchResultsPage() {
  const location = useLocation();
  const results = location.state?.results || [];

  return (
    <div className="search-results-page">
      <h2>Search Results</h2>
      {results.length === 0 ? (
        <p>No products found.</p>
      ) : (
        <div className="product-grid">
          {results.map(product => (
            <ProductCard key={product.productId} product={product} />
          ))}
        </div>
      )}
    </div>
  );
}
