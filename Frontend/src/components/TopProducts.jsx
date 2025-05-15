import React, { useEffect, useState } from "react";
import HorizontalScrollList from "./HorizontalScrollList";
import ProductCard from "./ProductCard";
import { fetchTopProducts } from "../api/product"; 


export default function TopProducts() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    const loadTopProducts = async () => {
      try {
        const topProducts = await fetchTopProducts();
        setProducts(topProducts);
      } catch (error) {
        console.error("Failed to fetch top products", error);
      }
    };

    loadTopProducts();
  }, []);

  return (
    <HorizontalScrollList
      title="Top Products"
      items={products}
      itemWidth={200}
      renderItem={(product) => <ProductCard product={product} />}
    />
  );
}
