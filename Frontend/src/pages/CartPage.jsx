import React, { useState } from "react";
import StoreCart from "../components/Cart/StoreCart";
import ShippingForm from "../components/Cart/ShippingForm";
import PaymentForm from "../components/Cart/PaymentForm";
import "../index.css";

const cartData = [
  {
    storeName: "Tech Store",
    products: [
      { id: 1, name: "Laptop", quantity: 1, price: 1200, image: "/assets/laptop.png" },
      { id: 2, name: "Mouse", quantity: 2, price: 25, image: "/assets/mouse.png" },
    ],
  },
  {
    storeName: "Book Shop",
    products: [
      { id: 3, name: "React Handbook", quantity: 1, price: 30, image: "/assets/book.png" },
    ],
  },
];

export default function CartPage() {
  const [cart, setCart] = useState(cartData);
  const [showShipping, setShowShipping] = useState(false);
  const [showPayment, setShowPayment] = useState(false);
  const [shippingDetails, setShippingDetails] = useState(null);
  const [paymentDetails, setPaymentDetails] = useState(null);

  const handleQuantityChange = (storeName, productId, newQuantity) => {
    const updatedCart = cart.map((store) => {
      if (store.storeName !== storeName) return store;
      return {
        ...store,
        products: store.products.map((product) =>
          product.productId === productId ? { ...product, quantity: newQuantity } : product
        ),
      };
    });
    setCart(updatedCart);
  };

  const handleRemoveProduct = (storeName, productId) => {
    const updatedCart = cart.map((store) => {
      if (store.storeName !== storeName) return store;
      return {
        ...store,
        products: store.products.filter((product) => product.productId !== productId),
      };
    });
    setCart(updatedCart);
  };

  const totalItems = cart.reduce((sum, store) => sum + store.products.length, 0);
  const totalQuantity = cart.reduce(
    (sum, store) => sum + store.products.reduce((prodSum, product) => prodSum + product.quantity, 0),
    0
  );
  const totalPrice = cart.reduce(
    (sum, store) =>
      sum + store.products.reduce((prodSum, product) => prodSum + product.quantity * product.price, 0),
    0
  );

  return (
    <div className="cart-page">
      <div className="cart-summary-row">
        <p className="cart-summary-text">
          You have {totalItems} products, {totalQuantity} items, ${totalPrice.toFixed(2)} in your cart
        </p>
        <button className="payment-btn" onClick={() => setShowPayment(!showPayment)}>
          Payment Method {paymentDetails && "✔"}
        </button>
        <button className="delivery-btn" onClick={() => setShowShipping(!showShipping)}>
          Shipping Address {shippingDetails && "✔"}
        </button>
        <button className="checkout-btn">Full Checkout</button>
      </div>

      {showShipping && (
        <ShippingForm
          onSubmit={(data) => {
            setShippingDetails(data);
            setShowShipping(false);
          }}
        />
      )}

      {showPayment && (
        <PaymentForm
          onSubmit={(data) => {
            setPaymentDetails(data);
            setShowPayment(false);
          }}
        />
      )}

      {cart.map((store, index) => (
        <StoreCart
          key={index}
          store={store}
          onQuantityChange={handleQuantityChange}
          onRemoveProduct={handleRemoveProduct}
        />
      ))}
    </div>
  );
}
