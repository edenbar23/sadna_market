import React from "react";
import '../../index.css';
import ProductRow from "./ProductRow";

export default function StoreCart({ store, onQuantityChange, onRemoveProduct }) {
  const { storeName, products } = store;

  const totalItems = products.length;
  const totalQuantity = products.reduce((sum, p) => sum + p.quantity, 0);
  const totalPrice = products.reduce((sum, p) => sum + p.quantity * p.price, 0);

  return (
    <div className="store-cart">
      <h2 className="store-title">{storeName}</h2>
      <p className="store-summary">
        {totalItems} products, {totalQuantity} items, ${totalPrice.toFixed(2)} total
      </p>
      <div className="product-list">
        {products.map((product) => (
          <ProductRow
            key={product.id}
            product={product}
            onQuantityChange={(newQty) => onQuantityChange(storeName, product.id, newQty)}
            onRemove={() => onRemoveProduct(storeName, product.id)}
          />
        ))}
        <button className="checkout-store-btn">Checkout Store</button>
      </div>
    </div>
  );
}
