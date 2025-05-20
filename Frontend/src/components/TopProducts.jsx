import React, { useEffect, useState } from "react";
import HorizontalScrollList from "./HorizontalScrollList";
import ProductCard from "./ProductCard";
import { fetchTopProducts } from "../api/product";
import "../index.css";

export default function TopProducts() {
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadTopProducts = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const response = await fetchTopProducts();

        if (response && response.data) {
          setProducts(response.data);
        } else {
          setProducts([]);
        }
      } catch (err) {
        console.error("Failed to fetch top products", err);
        setError("Failed to load top products");
      } finally {
        setIsLoading(false);
      }
    };

    loadTopProducts();
  }, []);

  if (isLoading) {
    return <div className="loading-indicator">Loading top products...</div>;
  }

  if (error) {
    return <div className="error-message">{error}</div>;
  }

  if (!products.length) {
    return <div className="no-data-message">No top products found</div>;
  }

  return (
      <HorizontalScrollList
          title="Top Products"
          items={products}
          renderItem={(product) => <ProductCard key={product.productId} product={product} />}
      />
  );
}