import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import { fetchStoreById, fetchStoreProducts } from "../api/store"; // Your mock or real API
import "../index.css";

export default function StorePage({ user }) {
  const { storeId } = useParams();
  const [store, setStore] = useState(null);
  const [products, setProducts] = useState([]);
//   const [hasCompletedOrder, setHasCompletedOrder] = useState(false);

useEffect(() => {
    const loadStoreData = async () => {
      try {
        const storeData = await fetchStoreById(storeId);
        const storeProducts = await fetchStoreProducts(storeId);
        setStore(storeData);
        setProducts(storeProducts);
      } catch (error) {
        console.error("Failed to load store data", error);
      }
    };

    loadStoreData();
}, [storeId]);
    
    
  if (!store) return <p>Loading store...</p>;

  return (
    <div className="store-page">
      <div className="store-header">
        <img src={store.logo} alt="Store Logo" className="store-logo" />
        <div>
          <h2>{store.name}</h2>
          <p>Rating: {store.rating} ⭐</p>
        </div>
      </div>

      <div className="store-actions">
        {user && (
          <>
            <button className="button">Message Seller</button>
            {/* {hasCompletedOrder && (
              <button className="button">Rate Store</button>
            )} */}
          </>
        )}
      </div>

      <div className="store-products-scroll">
        {products.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </div>
  );
}
