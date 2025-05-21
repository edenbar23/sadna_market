import { useEffect, useState } from "react";
import StoreCart from "../components/Cart/StoreCart";
import ShippingForm from "../components/Cart/ShippingForm";
import PaymentForm from "../components/Cart/PaymentForm";
import { useAuthContext } from "../context/AuthContext";
import {
  viewCartGuest,
  updateCartGuest,
  removeFromCartGuest,
  viewCart,
  updateCart,
  removeFromCart,
} from "../api/user"; // Make sure these API methods are correctly implemented and imported

// Detect user status (guest or logged in)
// const username = localStorage.getItem("username");
// const token = localStorage.getItem("token");


export default function CartPage() {
  const [cart, setCart] = useState([]);
  const { user, token } = useAuthContext();
  const username = user?.username;
  const [showShipping, setShowShipping] = useState(false);
  const [showPayment, setShowPayment] = useState(false);
  const [shippingDetails, setShippingDetails] = useState(null);
  const [paymentDetails, setPaymentDetails] = useState(null);
  const isGuest = !username || !token;

  // Fetch the cart from API or localStorage (for guest) on mount
  useEffect(() => {
    const fetchCart = async () => {
      try {
        if (isGuest) {
          const guestCart = localStorage.getItem("guestCart");
          if (guestCart) {
            setCart(JSON.parse(guestCart));
          } else {
            const response = await viewCartGuest();
            const basketsObj = response.data.baskets || {};
            const basketsArray = Object.values(basketsObj);
            setCart(basketsArray);
          }
        } else {
          const response = await viewCart(user.username, token);
          const basketsObj = response.data.baskets || {};
          const basketsArray = Object.values(basketsObj);
          setCart(basketsArray);
        }
      } catch (error) {
        console.error("Failed to load cart:", error);
      }
    };


    fetchCart();
  }, []);

  // Sync guest cart changes to localStorage
  useEffect(() => {
    if (isGuest) {
      localStorage.setItem("guestCart", JSON.stringify(cart));
    }
  }, [cart]);

  // Handle changing quantity of a product in a store's cart
  const handleQuantityChange = async (storeName, productId, newQuantity) => {
    const updatedCart = cart.map((store) => {
      if (store.storeName !== storeName) return store;
      return {
        ...store,
        products: store.products.map((product) =>
            product.productId === productId
                ? { ...product, quantity: newQuantity }
                : product
        ),
      };
    });

    setCart(updatedCart);

    try {
      if (isGuest) {
        await updateCartGuest(updatedCart, storeName, productId, newQuantity);
      } else {
        await updateCart(username, token, storeName, productId, newQuantity);
      }
    } catch (error) {
      console.error("Failed to update quantity:", error);
    }
  };

  // Handle removing a product from a store's cart
  const handleRemoveProduct = async (storeName, productId) => {
    const updatedCart = cart
        .map((store) => {
          if (store.storeName !== storeName) return store;
          return {
            ...store,
            products: store.products.filter(
                (product) => product.productId !== productId
            ),
          };
        })
        .filter((store) => store.products.length > 0); // Remove stores with no products

    setCart(updatedCart);

    try {
      if (isGuest) {
        await removeFromCartGuest(updatedCart, storeName, productId);
      } else {
        await removeFromCart(username, token, storeName, productId);
      }
    } catch (error) {
      console.error("Failed to remove product:", error);
    }
  };

  const totalItems = cart.reduce((count, store) => {
    return count + Object.keys(store.products ?? {}).length;
  }, 0);

  const totalQuantity = cart.reduce((sum, store) => {
    return sum + (store.totalQuantity ?? 0);
  }, 0);

  const totalPrice = cart.reduce((sum, store) => {
    return sum + Object.values(store.products ?? {}).reduce(
        (prodSum, quantity) => prodSum + quantity * 1, // $1 placeholder
        0
    );
  }, 0);



  return (
      <div className="cart-page">
        <div className="cart-summary-row">
          <p className="cart-summary-text">
            You have {totalItems} products, {totalQuantity} items, $
            {totalPrice.toFixed(2)} in your cart
          </p>
          <button
              className="payment-btn"
              onClick={() => setShowPayment((prev) => !prev)}
          >
            Payment Method {paymentDetails && "✔"}
          </button>
          <button
              className="delivery-btn"
              onClick={() => setShowShipping((prev) => !prev)}
          >
            Shipping Address {shippingDetails && "✔"}
          </button>
          <button className="checkout-btn" onClick={() => alert("Checkout logic here")}>
            Full Checkout
          </button>
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
                key={store.storeName || index}
                store={store}
                onQuantityChange={handleQuantityChange}
                onRemoveProduct={handleRemoveProduct}
            />
        ))}
      </div>
  );
}
