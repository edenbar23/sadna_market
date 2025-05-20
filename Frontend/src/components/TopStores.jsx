import React, { useEffect, useState } from "react";
import HorizontalScrollList from "./HorizontalScrollList";
import StoreCard from "./StoreCard";
import { fetchTopStores } from "../api/store";
import "../index.css";

export default function TopStores() {
  const [stores, setStores] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadTopStores = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const topStores = await fetchTopStores();
        setStores(topStores || []);
      } catch (error) {
        console.error("Failed to fetch top stores", error);
        setError("Failed to load top stores");
      } finally {
        setIsLoading(false);
      }
    };

    loadTopStores();
  }, []);

  if (isLoading) {
    return <div className="loading-indicator">Loading top stores...</div>;
  }

  if (error) {
    return <div className="error-message">{error}</div>;
  }

  if (!stores.length) {
    return <div className="no-data-message">No top stores found</div>;
  }

  return (
      <HorizontalScrollList
          title="Top Stores"
          items={stores}
          renderItem={(store) => <StoreCard store={store} />}
      />
  );
}