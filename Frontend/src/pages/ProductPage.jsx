import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getProductInfo, getStoreProducts, rateProduct } from "../api/product";
import StoreActionPanel from "../components/StoreActionPanel";
import ProductCard from "../components/ProductCard";
import ProductInfo from "../components/ProductInfo";
import { useAuthContext } from "../context/AuthContext";
import "../index.css";

export default function ProductPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const { user, token } = useAuthContext();

  const [product, setProduct] = useState(null);
  const [store, setStore] = useState(null);
  const [relatedProducts, setRelatedProducts] = useState([]);
  const [similarProducts, setSimilarProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProductData = async () => {
      if (!productId) return;

      setIsLoading(true);
      setError(null);

      try {
        // Fetch product details
        const productResponse = await getProductInfo(productId);

        if (productResponse && !productResponse.error) {
          const productData = productResponse.data;
          setProduct(productData);

          // If product has storeId, fetch store info and related products
          if (productData.storeId) {
            // Fetch store products (for related products)
            const storeProductsResponse = await getStoreProducts(productData.storeId);

            if (storeProductsResponse && !storeProductsResponse.error) {
              const storeProducts = storeProductsResponse.data || [];

              // Filter out the current product
              const otherProducts = storeProducts.filter(p => p.productId !== productId);

              // Set related products (from same store)
              setRelatedProducts(otherProducts.slice(0, 4));

              // Set similar products (same category)
              if (productData.category) {
                const similarProductsList = storeProducts.filter(
                    p => p.productId !== productId && p.category === productData.category
                );
                setSimilarProducts(similarProductsList.slice(0, 4));
              }
            }

            // Set store info from product data
            setStore({
              id: productData.storeId,
              name: productData.storeName || "Store",
              rating: productData.storeRating || 0,
              logo: "/assets/blank_store.png" // Default logo if not available
            });
          }
        }
      } catch (err) {
        console.error("Error fetching product data:", err);
        setError("Failed to load product information. Please try again later.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchProductData();
  }, [productId]);

  const handleRateProduct = async (rating) => {
    if (!user || !token) {
      // Prompt user to login
      alert("Please login to rate products");
      return;
    }

    try {
      const rateData = {
        productId,
        username: user.username,
        rating
      };

      await rateProduct(token, rateData);

      // Update the product rating in the UI
      setProduct(prev => ({
        ...prev,
        rating
      }));

      alert("Thank you for rating this product!");
    } catch (err) {
      console.error("Failed to rate product:", err);
      alert("Failed to submit rating. Please try again.");
    }
  };

  if (isLoading) {
    return <div className="loading-indicator">Loading product information...</div>;
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

  if (!product) {
    return <div className="not-found">Product not found</div>;
  }

  return (
      <div className="product-page">
        <div className="product-main">
          <ProductInfo
              product={product}
              onRate={handleRateProduct}
              canRate={!!user}
          />
          {store && <StoreActionPanel store={store} />}
        </div>

        {similarProducts.length > 0 && (
            <div className="related-products">
              <h2>Similar Products</h2>
              <div className="horizontal-scroll">
                {similarProducts.map((p) => (
                    <ProductCard key={p.productId} product={p} />
                ))}
              </div>
            </div>
        )}

        {relatedProducts.length > 0 && (
            <div className="related-products">
              <h2>Other Products from {store?.name}</h2>
              <div className="horizontal-scroll">
                {relatedProducts.map((p) => (
                    <ProductCard key={p.productId} product={p} />
                ))}
              </div>
            </div>
        )}
      </div>
  );
}