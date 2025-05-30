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
} from "../api/user";

export default function CartPage() {
  const [cart, setCart] = useState({ baskets: {}, totalItems: 0, totalPrice: 0 });
  const { user, token } = useAuthContext();
  const username = user?.username;
  const [showShipping, setShowShipping] = useState(false);
  const [showPayment, setShowPayment] = useState(false);
  const [shippingDetails, setShippingDetails] = useState(null);
  const [paymentDetails, setPaymentDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const isGuest = !username || !token;

  // Fetch the cart from API
  useEffect(() => {
    const fetchCart = async () => {
      setLoading(true);
      setError(null);

      try {
        if (isGuest) {
          // For guest users, use localStorage
          const guestCart = localStorage.getItem("guestCart");
          if (guestCart) {
            const parsedCart = JSON.parse(guestCart);
            const transformedCart = {
              baskets: {},
              totalItems: 0,
              totalPrice: 0
            };

            Object.entries(parsedCart.baskets || {}).forEach(([storeId, products]) => {
              transformedCart.baskets[storeId] = {
                storeId,
                storeName: `Store ${storeId.substring(0, 8)}...`,
                products: {},
                totalQuantity: 0,
                totalPrice: 0
              };

              Object.entries(products).forEach(([productId, quantity]) => {
                transformedCart.baskets[storeId].products[productId] = {
                  productId,
                  name: `Product ${productId.substring(0, 8)}...`,
                  price: 10.00,
                  quantity,
                  image: "/assets/blank_product.png"
                };
                transformedCart.baskets[storeId].totalQuantity += quantity;
                transformedCart.baskets[storeId].totalPrice += 10.00 * quantity;
                transformedCart.totalItems += quantity;
                transformedCart.totalPrice += 10.00 * quantity;
              });
            });

            setCart(transformedCart);
          }
        } else {
          // For registered users, fetch from backend
          const response = await viewCart(username, token);
          console.log("Cart response:", response);

          if (response && response.data) {
            setCart({
              baskets: response.data.baskets || {},
              totalItems: response.data.totalItems || 0,
              totalPrice: response.data.totalPrice || 0
            });
          }
        }
      } catch (error) {
        console.error("Failed to load cart:", error);
        setError("Failed to load cart");
        setCart({ baskets: {}, totalItems: 0, totalPrice: 0 });
      } finally {
        setLoading(false);
      }
    };

    fetchCart();
  }, [isGuest, username, token]);

  // Handle changing quantity of a product in a store's cart
  const handleQuantityChange = async (storeId, productId, newQuantity) => {
    try {
      if (isGuest) {
        // Handle guest cart update
        const guestCart = JSON.parse(localStorage.getItem("guestCart") || '{"baskets": {}}');
        if (guestCart.baskets[storeId] && guestCart.baskets[storeId][productId]) {
          guestCart.baskets[storeId][productId] = newQuantity;
          localStorage.setItem("guestCart", JSON.stringify(guestCart));

          // Update local state
          setCart(prevCart => {
            const newCart = { ...prevCart };
            if (newCart.baskets[storeId] && newCart.baskets[storeId].products[productId]) {
              const oldQuantity = newCart.baskets[storeId].products[productId].quantity;
              const price = newCart.baskets[storeId].products[productId].price;

              newCart.baskets[storeId].products[productId].quantity = newQuantity;
              newCart.baskets[storeId].totalQuantity += (newQuantity - oldQuantity);
              newCart.baskets[storeId].totalPrice += (price * (newQuantity - oldQuantity));
              newCart.totalItems += (newQuantity - oldQuantity);
              newCart.totalPrice += (price * (newQuantity - oldQuantity));
            }
            return newCart;
          });
        }
      } else {
        // Handle registered user cart update
        const response = await updateCart(username, token, storeId, productId, newQuantity);
        if (response && response.data) {
          setCart({
            baskets: response.data.baskets || {},
            totalItems: response.data.totalItems || 0,
            totalPrice: response.data.totalPrice || 0
          });
        }
      }
    } catch (error) {
      console.error("Failed to update quantity:", error);
      setError("Failed to update quantity");
    }
  };

  // Handle removing a product from a store's cart
  const handleRemoveProduct = async (storeId, productId) => {
    try {
      if (isGuest) {
        // Handle guest cart removal
        const guestCart = JSON.parse(localStorage.getItem("guestCart") || '{"baskets": {}}');
        if (guestCart.baskets[storeId] && guestCart.baskets[storeId][productId]) {
          delete guestCart.baskets[storeId][productId];
          if (Object.keys(guestCart.baskets[storeId]).length === 0) {
            delete guestCart.baskets[storeId];
          }
          localStorage.setItem("guestCart", JSON.stringify(guestCart));

          // Update local state
          setCart(prevCart => {
            const newCart = { ...prevCart };
            if (newCart.baskets[storeId] && newCart.baskets[storeId].products[productId]) {
              const product = newCart.baskets[storeId].products[productId];
              newCart.baskets[storeId].totalQuantity -= product.quantity;
              newCart.baskets[storeId].totalPrice -= (product.price * product.quantity);
              newCart.totalItems -= product.quantity;
              newCart.totalPrice -= (product.price * product.quantity);

              delete newCart.baskets[storeId].products[productId];

              // Remove store if no products left
              if (Object.keys(newCart.baskets[storeId].products).length === 0) {
                delete newCart.baskets[storeId];
              }
            }
            return newCart;
          });
        }
      } else {
        // Handle registered user cart removal
        const response = await removeFromCart(username, token, storeId, productId);
        if (response && response.data) {
          setCart({
            baskets: response.data.baskets || {},
            totalItems: response.data.totalItems || 0,
            totalPrice: response.data.totalPrice || 0
          });
        }
      }
    } catch (error) {
      console.error("Failed to remove product:", error);
      setError("Failed to remove product");
    }
  };

  if (loading) {
    return <div className="cart-page">Loading cart...</div>;
  }

  if (error) {
    return <div className="cart-page">Error: {error}</div>;
  }

  const cartArray = Object.values(cart.baskets || {});

  return (
      <div className="cart-page">
        <div className="cart-summary-row">
          <p className="cart-summary-text">
            You have {Object.keys(cart.baskets || {}).length} stores, {cart.totalItems} items, $
            {cart.totalPrice.toFixed(2)} in your cart
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
                onComplete={(data) => {
                  setShippingDetails(data);
                  setShowShipping(false);
                }}
            />
        )}

        {showPayment && (
            <PaymentForm
                onComplete={(data) => {
                  setPaymentDetails(data);
                  setShowPayment(false);
                }}
            />
        )}

        {cartArray.length === 0 ? (
            <div className="empty-cart">
              <p>Your cart is empty</p>
            </div>
        ) : (
            cartArray.map((store) => (
                <StoreCart
                    key={store.storeId}
                    store={store}
                    onQuantityChange={handleQuantityChange}
                    onRemoveProduct={handleRemoveProduct}
                />
            ))
        )}
      </div>
  );
}