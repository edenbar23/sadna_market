import React from "react";
import ProductCard from "../components/ProductCard";
import StoreCard from "../components/StoreCard";
import SearchBar from "../components/SearchBar"; 
import "../index.css";

export default function SearchResultsPage({ results }) {
  const { products = [], stores = [] } = results || {};

  return (
    <div className="container">
      <SearchBar /> 
          <br />

      <h1>Search Results</h1>

      {stores.length > 0 && (
        <>
          <h2>Stores</h2>
          <div className="store-results">
            {stores.map((store) => (
              <StoreCard key={store.id} store={store} />
            ))}
          </div>
        </>
      )}

      {products.length > 0 && (
        <>
          <h2>Products</h2>
          <div className="product-results">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        </>
      )}

      {products.length === 0 && stores.length === 0 && (
        <p className="no-results">No results found for your query.</p>
      )}
    </div>
  );
}
