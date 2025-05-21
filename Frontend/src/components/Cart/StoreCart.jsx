import React from "react";
import "../../index.css";
import ProductRow from "./ProductRow";

export default function StoreCart({ store, onQuantityChange, onRemoveProduct }) {
    const { storeName } = store;

    // Normalize products to array if it's an object
    const productsArray = Array.isArray(store.products)
        ? store.products
        : Object.values(store.products ?? {});

    const totalItems = productsArray.length;
    const totalQuantity = productsArray.reduce((sum, p) => sum + (p.quantity || 0), 0);
    const totalPrice = productsArray.reduce((sum, p) => sum + (p.quantity || 0) * (p.price || 0), 0);

    return (
        <div className="store-cart">
            <h2 className="store-title">{storeName}</h2>
            <p className="store-summary">
                {totalItems} products, {totalQuantity} items, ${totalPrice.toFixed(2)} total
            </p>
            <div className="product-list">
                {productsArray.map((product) => (
                    <ProductRow
                        key={product.productId}
                        product={product}
                        onQuantityChange={(newQty) =>
                            onQuantityChange(storeName, product.productId, newQty)
                        }
                        onRemove={() => onRemoveProduct(storeName, product.productId)}
                    />
                ))}
                <button className="checkout-store-btn">Checkout Store</button>
            </div>
        </div>
    );
}
