import React from "react";
import "../../index.css";
import ProductRow from "./ProductRow";

export default function StoreCart({ store, onQuantityChange, onRemoveProduct }) {
    const { storeId, storeName, products, totalQuantity, totalPrice } = store;

    // Convert products object to array for rendering
    const productsArray = Object.values(products || {});

    const handleQuantityChange = (productId, newQuantity) => {
        onQuantityChange(storeId, productId, newQuantity);
    };

    const handleRemoveProduct = (productId) => {
        onRemoveProduct(storeId, productId);
    };

    if (productsArray.length === 0) {
        return null; // Don't render empty stores
    }

    return (
        <div className="store-cart">
            <h2 className="store-title">{storeName || `Store ${storeId.substring(0, 8)}...`}</h2>
            <p className="store-summary">
                {productsArray.length} products, {totalQuantity || 0} items, ${(totalPrice || 0).toFixed(2)} total
            </p>
            <div className="product-list">
                {productsArray.map((product) => (
                    <ProductRow
                        key={product.productId}
                        product={product}
                        onQuantityChange={(newQty) => handleQuantityChange(product.productId, newQty)}
                        onRemove={() => handleRemoveProduct(product.productId)}
                    />
                ))}
                <button className="checkout-store-btn">Checkout Store</button>
            </div>
        </div>
    );
}