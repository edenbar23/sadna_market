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
import "../styles/cart.css"

export default function CartPage() {
  const [cart, setCart] = useState({ baskets: {}, totalItems: 0, totalPrice: 0 });
  const [selectedProducts, setSelectedProducts] = useState([]);
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
          const response = await viewCart(username, token);
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

  // Selection handlers
  const handleProductSelectionChange = (productId, isSelected) => {
    setSelectedProducts(prev => {
      if (isSelected) {
        return [...prev, productId];
      } else {
        return prev.filter(id => id !== productId);
      }
    });
  };

  const handleSelectAllStore = (storeId, productIds, isSelected) => {
    setSelectedProducts(prev => {
      if (isSelected) {
        const newSelected = [...prev];
        productIds.forEach(productId => {
          if (!newSelected.includes(productId)) {
            newSelected.push(productId);
          }
        });
        return newSelected;
      } else {
        return prev.filter(productId => !productIds.includes(productId));
      }
    });
  };

  const handleSelectAll = (isSelected) => {
    if (isSelected) {
      const allProductIds = [];
      Object.values(cart.baskets).forEach(store => {
        Object.keys(store.products).forEach(productId => {
          allProductIds.push(productId);
        });
      });
      setSelectedProducts(allProductIds);
    } else {
      setSelectedProducts([]);
    }
  };

  // Calculate selected items statistics
  const calculateSelectedStats = () => {
    let totalSelectedItems = 0;
    let totalSelectedPrice = 0;

    selectedProducts.forEach(productId => {
      Object.values(cart.baskets).forEach(store => {
        const product = store.products[productId];
        if (product) {
          totalSelectedItems += product.quantity;
          totalSelectedPrice += product.price * product.quantity;
        }
      });
    });

    return { totalSelectedItems, totalSelectedPrice };
  };

  const { totalSelectedItems, totalSelectedPrice } = calculateSelectedStats();

  // Existing handlers
  const handleQuantityChange = async (storeId, productId, newQuantity) => {
    try {
      if (isGuest) {
        const guestCart = JSON.parse(localStorage.getItem("guestCart") || '{"baskets": {}}');
        if (guestCart.baskets[storeId] && guestCart.baskets[storeId][productId]) {
          guestCart.baskets[storeId][productId] = newQuantity;
          localStorage.setItem("guestCart", JSON.stringify(guestCart));

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

  const handleRemoveProduct = async (storeId, productId) => {
    try {
      setSelectedProducts(prev => prev.filter(id => id !== productId));

      if (isGuest) {
        const guestCart = JSON.parse(localStorage.getItem("guestCart") || '{"baskets": {}}');
        if (guestCart.baskets[storeId] && guestCart.baskets[storeId][productId]) {
          delete guestCart.baskets[storeId][productId];
          if (Object.keys(guestCart.baskets[storeId]).length === 0) {
            delete guestCart.baskets[storeId];
          }
          localStorage.setItem("guestCart", JSON.stringify(guestCart));

          setCart(prevCart => {
            const newCart = { ...prevCart };
            if (newCart.baskets[storeId] && newCart.baskets[storeId].products[productId]) {
              const product = newCart.baskets[storeId].products[productId];
              newCart.baskets[storeId].totalQuantity -= product.quantity;
              newCart.baskets[storeId].totalPrice -= (product.price * product.quantity);
              newCart.totalItems -= product.quantity;
              newCart.totalPrice -= (product.price * product.quantity);

              delete newCart.baskets[storeId].products[productId];

              if (Object.keys(newCart.baskets[storeId].products).length === 0) {
                delete newCart.baskets[storeId];
              }
            }
            return newCart;
          });
        }
      } else {
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

  // Checkout handlers
  const handleCheckoutAll = () => {
    alert("Checkout all items logic here");
  };

  const handleCheckoutSelected = () => {
    if (selectedProducts.length === 0) {
      alert("Please select items to checkout");
      return;
    }
    alert(`Checkout ${selectedProducts.length} selected items for $${totalSelectedPrice.toFixed(2)}`);
  };

  const handleCheckoutStore = (storeId, productIds) => {
    const store = cart.baskets[storeId];
    if (productIds) {
      alert(`Checkout selected items from ${store.storeName}`);
    } else {
      alert(`Checkout entire store: ${store.storeName} for $${store.totalPrice.toFixed(2)}`);
    }
  };

  if (loading) {
    return <div className="cart-page">Loading cart...</div>;
  }

  if (error) {
    return <div className="cart-page">Error: {error}</div>;
  }

  const cartArray = Object.values(cart.baskets || {});
  const allProductsSelected = selectedProducts.length > 0 &&
      Object.values(cart.baskets).every(store =>
          Object.keys(store.products).every(productId => selectedProducts.includes(productId))
      );

  return (
      <div className="cart-page">
        {cart.totalItems > 0 ? (
            <>
              <div className="cart-header">
                <div className="cart-title-section">
                  <h1 className="cart-title">Shopping Cart</h1>
                  <div className="select-all-container">
                    <input
                        type="checkbox"
                        id="select-all"
                        checked={allProductsSelected}
                        onChange={(e) => handleSelectAll(e.target.checked)}
                        className="select-all-checkbox"
                    />
                    <label htmlFor="select-all" className="select-all-label">
                      Select All Items
                    </label>
                  </div>
                </div>

                <div className="cart-summary">
                  <div className="cart-totals">
                    <div className="total-section">
                      <span className="total-label">Total:</span>
                      <span className="total-stores">{Object.keys(cart.baskets || {}).length} stores</span>
                      <span className="total-items">{cart.totalItems} items</span>
                      <span className="total-price">${cart.totalPrice.toFixed(2)}</span>
                    </div>

                    {selectedProducts.length > 0 && (
                        <div className="selected-section">
                          <span className="selected-label">Selected:</span>
                          <span className="selected-items">{totalSelectedItems} items</span>
                          <span className="selected-price">${totalSelectedPrice.toFixed(2)}</span>
                        </div>
                    )}
                  </div>

                  <div className="cart-actions">
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
                    <button
                        className="checkout-btn"
                        onClick={handleCheckoutAll}
                        disabled={cart.totalItems === 0}
                    >
                      Checkout All (${cart.totalPrice.toFixed(2)})
                    </button>
                    {selectedProducts.length > 0 && (
                        <button
                            className="checkout-selected-btn"
                            onClick={handleCheckoutSelected}
                        >
                          Checkout Selected ({selectedProducts.length} items - ${totalSelectedPrice.toFixed(2)})
                        </button>
                    )}
                  </div>
                </div>
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

              <div className="cart-content">
                {cartArray.map((store) => (
                    <StoreCart
                        key={store.storeId}
                        store={store}
                        selectedProducts={selectedProducts}
                        onProductSelectionChange={handleProductSelectionChange}
                        onSelectAllStore={handleSelectAllStore}
                        onQuantityChange={handleQuantityChange}
                        onRemoveProduct={handleRemoveProduct}
                        onCheckoutStore={handleCheckoutStore}
                    />
                ))}
              </div>
            </>
        ) : (
            /* Simple Empty Cart - No Recommendations */
            <div className="cart-page">
              <div className="empty-cart">
                <div className="empty-cart-icon">🛒</div>
                <h2>Your cart is empty</h2>
                <p>Add some products to get started!</p>
                <div style={{ marginTop: '2rem' }}>
                  <button
                      onClick={() => window.location.href = '/'}
                      style={{
                        padding: '1rem 2rem',
                        background: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '8px',
                        cursor: 'pointer',
                        fontSize: '1rem',
                        fontWeight: '600'
                      }}
                  >
                    Continue Shopping
                  </button>
                </div>
              </div>
            </div>
        )}
      </div>
  );
}