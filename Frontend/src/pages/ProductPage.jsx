import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import StoreActionPanel from "../components/StoreActionPanel";
import ProductCard from "../components/ProductCard";
import ProductInfo from "../components/ProductInfo";
import "../index.css";

export default function ProductPage() {
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const [store, setStore] = useState(null);
  const [relatedProducts, setRelatedProducts] = useState([]);
  const [similarProducts, setSimilarProducts] = useState([]);

  useEffect(() => {
    // Simulate backend fetch
    const fetchedProduct = {
      id: productId,
      name: "Wireless Headphones",
      price: 199.99,
      rating: 4.5,
      description: "High quality wireless headphones with noise cancellation.",
      type: "immediate", // "bid", "lottery"
      image: "/assets/headphones.png",
      category: "Audio",
      store: {
        id: "s1",
        name: "AudioWorld",
        rating: 4.8,
        logo: "/assets/audioworld-logo.png",
      },
    };

    const otherProducts = [
      { id: "p2", name: "Bluetooth Speaker", price: 99.99, image: "/assets/speaker.png" },
      { id: "p3", name: "Soundbar", price: 299.99, image: "/assets/soundbar.png" },
    ];

    const similarProductsData = [
      { id: "p4", name: "Noise Cancelling Earbuds", price: 149.99, image: "/assets/earbuds.png" },
      { id: "p5", name: "Studio Monitor Headphones", price: 249.99, image: "/assets/monitor-headphones.png" },
    ];

    setProduct(fetchedProduct);
    setStore(fetchedProduct.store);
    setRelatedProducts(otherProducts);
    setSimilarProducts(similarProductsData);
  }, [productId]);

  if (!product || !store) return <div>Loading...</div>;

  return (
    <div className="product-page">
      <div className="product-main">
        <ProductInfo product={product} />
        <StoreActionPanel store={store} />
      </div>

      {similarProducts.length > 0 && (
        <div className="related-products">
          <h2>Similar Products</h2>
          <div className="horizontal-scroll">
            {similarProducts.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </div>
      )}

      {relatedProducts.length > 0 && (
        <div className="related-products">
          <h2>Other Products from {store.name}</h2>
          <div className="horizontal-scroll">
            {relatedProducts.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
