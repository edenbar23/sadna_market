import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getStoreProducts } from "../api/product";
import { fetchStoreById } from "../api/store";
import ProductCard from "../components/ProductCard";
import StoreActionPanel from "../components/StoreActionPanel";
import SearchBar from "../components/SearchBar";
import "../index.css";

export default function StorePage() {
    const { storeId } = useParams();
    const navigate = useNavigate();

    const [store, setStore] = useState(null);
    const [products, setProducts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchStoreData = async () => {
            if (!storeId) return;

            setIsLoading(true);
            setError(null);

            try {
                // Fetch store details
                const storeResponse = await fetchStoreById(storeId);

                if (storeResponse && !storeResponse.error) {
                    setStore(storeResponse);

                    // Fetch store products in parallel
                    const productsResponse = await getStoreProducts(storeId);

                    if (productsResponse && !productsResponse.error) {
                        setProducts(productsResponse.data || []);
                    }
                }
            } catch (err) {
                console.error("Error fetching store data:", err);
                setError("Failed to load store information. Please try again later.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchStoreData();
    }, [storeId]);

    if (isLoading) {
        return <div className="loading-indicator">Loading store information...</div>;
    }

    if (error) {
        return (
            <div className="error-container">
                <h2>Error</h2>
                <p>{error}</p>
                <button onClick={() => navigate(-1)}>Go Back</button>
            </div>
        );
    }

    if (!store) {
        return <div className="not-found">Store not found</div>;
    }

    return (
        <div className="store-page">
            <SearchBar storeId={storeId} />
            <div>
                    <StoreActionPanel store={store} />
            </div>



            <h2 className="store-products-title">Store Products</h2>
            <div className="store-products-scroll">
                {products.length > 0 ? (
                    products.map(product => (
                        <ProductCard key={product.productId} product={product} />
                    ))
                ) : (
                    <p>No products available in this store.</p>
                )}
            </div>
        </div>
    );
}