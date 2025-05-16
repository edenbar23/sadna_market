import React, { useEffect, useState } from "react";
import HorizontalScrollList from "./HorizontalScrollList";
import StoreCard from "./StoreCard";
import { fetchTopStores } from "../api/store"; 


export default function TopStores() {
  const [stores, setStores] = useState([]);

  useEffect(() => {
    const loadTopStores = async () => {
      try {
        const topStores = await fetchTopStores();
        setStores(topStores);
      } catch (error) {
        console.error("Failed to fetch top stores", error);
      }
    };

    loadTopStores();
  }, []);

  return (
    <HorizontalScrollList
      title="Top Stores"
      items={stores}
      itemWidth={200}
      renderItem={(store) => <StoreCard store={store} />}
    />
  );
}
