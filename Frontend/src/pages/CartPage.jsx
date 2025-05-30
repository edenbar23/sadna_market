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
  checkout,
  checkoutGuest,
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

  // Helper functions for checkout
  const createPaymentMethod = (paymentDetails) => {
    return {
      type: "creditCard",
      cardNumber: paymentDetails.cardNumber,
      expiryDate: paymentDetails.expiryDate,
      cvv: paymentDetails.cvv,
      cardHolderName: paymentDetails.cardHolder
    };
  };

  const createSupplyMethod = (shippingDetails) => {
    return {
      type: "standardShipping",
      carrier: "Standard",
      estimatedDays: 3,
    };
  };

  const formatShippingAddress = (shippingDetails) => {
    if (shippingDetails.type === 'guest') {
      const address = shippingDetails.data;
      return `${address.fullName}, ${address.address}, ${address.city}, ${address.postalCode}`;
    } else {
      const address = shippingDetails.data;
      return `${address.fullName}, ${address.addressLine1}, ${address.city}, ${address.state} ${address.postalCode}`;
    }
  };

  const formatGuestCartItems = (cart) => {
    const formattedItems = {};

    Object.entries(cart.baskets).forEach(([storeId, storeData]) => {
      formattedItems[storeId] = {};

      Object.entries(storeData.products).forEach(([productId, productData]) => {
        formattedItems[storeId][productId] = productData.quantity;
      });
    });

    return formattedItems;
  };

  const prepareCheckoutData = (cart, shippingDetails, paymentDetails) => {
    // For registered users
    if (!isGuest) {
      return {
        paymentMethod: createPaymentMethod(paymentDetails),
        supplyMethod: createSupplyMethod(shippingDetails),
        shippingAddress: formatShippingAddress(shippingDetails), // Address goes here, not in supply method
        deliveryInstructions: ""
      };
    }
    // For guests
    else {
      return {
        cartItems: formatGuestCartItems(cart),
        paymentMethod: createPaymentMethod(paymentDetails),
        supplyMethod: createSupplyMethod(shippingDetails),
        shippingAddress: formatShippingAddress(shippingDetails), // Address goes here, not in supply method
        contactEmail: shippingDetails.data.email || "guest@example.com",
        contactPhone: shippingDetails.data.phone || "",
        deliveryInstructions: ""
      };
    }
  };

  const filterCartBySelectedProducts = (cart, selectedProducts) => {
    const filteredCart = { baskets: {}, totalItems: 0, totalPrice: 0 };

    Object.entries(cart.baskets).forEach(([storeId, storeData]) => {
      const filteredProducts = {};
      let storeTotalQuantity = 0;
      let storeTotalPrice = 0;

      Object.entries(storeData.products).forEach(([productId, productData]) => {
        if (selectedProducts.includes(productId)) {
          filteredProducts[productId] = productData;
          storeTotalQuantity += productData.quantity;
          storeTotalPrice += productData.price * productData.quantity;
          filteredCart.totalItems += productData.quantity;
          filteredCart.totalPrice += productData.price * productData.quantity;
        }
      });

      if (Object.keys(filteredProducts).length > 0) {
        filteredCart.baskets[storeId] = {
          ...storeData,
          products: filteredProducts,
          totalQuantity: storeTotalQuantity,
          totalPrice: storeTotalPrice
        };
      }
    });

    return filteredCart;
  };

  // Checkout handlers
  const handleCheckoutAll = async () => {
    if (!shippingDetails) {
      alert("Please provide shipping information");
      setShowShipping(true);
      return;
    }

    if (!paymentDetails) {
      alert("Please provide payment information");
      setShowPayment(true);
      return;
    }

    try {
      setLoading(true);

      // Prepare checkout data
      const checkoutData = prepareCheckoutData(cart, shippingDetails, paymentDetails);

      console.log("Sending checkout data:", checkoutData); // Debug log

      // Call appropriate API based on user status
      let response;
      if (isGuest) {
        response = await checkoutGuest(checkoutData);
      } else {
        response = await checkout(username, token, checkoutData);
      }

      console.log("Checkout response:", response); // Debug log

      // FIXED: Handle your Response<CheckoutResultDTO> structure
      if (response && !response.error && response.data) {
        const checkoutResult = response.data;

        // Debug: Log the entire response to see the structure
        console.log("Full checkout response:", response);
        console.log("Checkout result data:", checkoutResult);

        // FIXED: Handle orderIds as List<String> from CheckoutResultDTO
        let orderIds = [];

        if (checkoutResult.orderIds && Array.isArray(checkoutResult.orderIds) && checkoutResult.orderIds.length > 0) {
          orderIds = checkoutResult.orderIds;
        }

        if (orderIds.length === 0) {
          console.error("No order IDs found in response:", checkoutResult);
          console.error("orderIds field:", checkoutResult.orderIds);
          alert("Checkout completed but no order IDs found. Please check your order history.");
          return;
        }

        console.log("Order IDs created:", orderIds);

        // Clear cart
        setCart({ baskets: {}, totalItems: 0, totalPrice: 0 });

        // Clear local storage for guest cart if needed
        if (isGuest) {
          localStorage.removeItem("guestCart");
        }

        // Show success message with all order IDs
        if (orderIds.length === 1) {
          alert(`Checkout successful! Order ID: ${orderIds[0]}`);
          // Redirect to single order confirmation
          window.location.href = `/order-confirmation/${orderIds[0]}`;
        } else {
          // Multiple orders created
          const orderList = orderIds.map((id, index) => `${index + 1}. ${id}`).join('\n');
          alert(`Checkout successful! ${orderIds.length} orders created:\n\n${orderList}\n\nYou can view all orders in your order history.`);
          // Redirect to order history instead of single order
          window.location.href = `/orders`;
        }
      } else {
        // Handle error response from your Response<T> structure
        const errorMessage = response.errorMessage || "Unknown error occurred";
        throw new Error(errorMessage);
      }
    } catch (error) {
      console.error("Checkout failed:", error);

      // Extract error message
      let errorMessage = "Unknown error";
      if (error.response?.data?.errorMessage) {
        // Your Response<T> error structure
        errorMessage = error.response.data.errorMessage;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      alert(`Checkout failed: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  const handleCheckoutSelected = async () => {
    if (selectedProducts.length === 0) {
      alert("Please select items to checkout");
      return;
    }

    if (!shippingDetails) {
      alert("Please provide shipping information");
      setShowShipping(true);
      return;
    }

    if (!paymentDetails) {
      alert("Please provide payment information");
      setShowPayment(true);
      return;
    }

    try {
      setLoading(true);

      // Filter cart to only include selected products
      const filteredCart = filterCartBySelectedProducts(cart, selectedProducts);

      // Prepare checkout data with filtered cart
      const checkoutData = prepareCheckoutData(filteredCart, shippingDetails, paymentDetails);

      // Call appropriate API based on user status
      let response;
      if (isGuest) {
        response = await checkoutGuest(checkoutData);
      } else {
        response = await checkout(username, token, checkoutData);
      }

      // FIXED: Handle your Response<CheckoutResultDTO> structure
      if (response && !response.error && response.data) {
        const checkoutResult = response.data;

        // Debug: Log the entire response to see the structure
        console.log("Full checkout response:", response);
        console.log("Checkout result data:", checkoutResult);

        // FIXED: Handle orderIds as List<String> from CheckoutResultDTO
        let orderId = null;

        if (checkoutResult.orderIds && Array.isArray(checkoutResult.orderIds) && checkoutResult.orderIds.length > 0) {
          // Use the first order ID if multiple orders were created
          orderId = checkoutResult.orderIds[0];

          if (checkoutResult.orderIds.length > 1) {
            console.log("Multiple orders created:", checkoutResult.orderIds);
            // You might want to handle multiple orders differently
          }
        }

        if (!orderId) {
          console.error("No order IDs found in response:", checkoutResult);
          console.error("orderIds field:", checkoutResult.orderIds);
          alert("Checkout completed but no order IDs found. Please check your order history.");
          return;
        }

        console.log("Using order ID:", orderId);

        // Remove checked out items from cart
        const updatedCart = { ...cart };
        selectedProducts.forEach(productId => {
          Object.keys(updatedCart.baskets).forEach(storeId => {
            if (updatedCart.baskets[storeId].products[productId]) {
              const product = updatedCart.baskets[storeId].products[productId];
              updatedCart.baskets[storeId].totalQuantity -= product.quantity;
              updatedCart.baskets[storeId].totalPrice -= product.price * product.quantity;
              updatedCart.totalItems -= product.quantity;
              updatedCart.totalPrice -= product.price * product.quantity;
              delete updatedCart.baskets[storeId].products[productId];

              // Remove store if empty
              if (Object.keys(updatedCart.baskets[storeId].products).length === 0) {
                delete updatedCart.baskets[storeId];
              }
            }
          });
        });

        setCart(updatedCart);
        setSelectedProducts([]);

        // Update local storage for guest cart if needed
        if (isGuest) {
          const guestCart = { baskets: {} };
          Object.entries(updatedCart.baskets).forEach(([storeId, storeData]) => {
            guestCart.baskets[storeId] = {};
            Object.entries(storeData.products).forEach(([productId, productData]) => {
              guestCart.baskets[storeId][productId] = productData.quantity;
            });
          });
          localStorage.setItem("guestCart", JSON.stringify(guestCart));
        }

        // Show success message
        alert(`Checkout successful! Order ID: ${orderId}`);

        // Redirect to order confirmation page
        window.location.href = `/order-confirmation/${orderId}`;
      } else {
        const errorMessage = response.errorMessage || "Unknown error occurred";
        throw new Error(errorMessage);
      }
    } catch (error) {
      console.error("Checkout failed:", error);

      let errorMessage = "Unknown error";
      if (error.response?.data?.errorMessage) {
        errorMessage = error.response.data.errorMessage;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      alert(`Checkout failed: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  const handleCheckoutStore = async (storeId, selectedProductIds) => {
    const store = cart.baskets[storeId];

    if (!shippingDetails) {
      alert("Please provide shipping information");
      setShowShipping(true);
      return;
    }

    if (!paymentDetails) {
      alert("Please provide payment information");
      setShowPayment(true);
      return;
    }

    try {
      setLoading(true);

      // Create a filtered cart with only the selected store
      const filteredCart = { baskets: {}, totalItems: 0, totalPrice: 0 };
      filteredCart.baskets[storeId] = { ...store };

      // If specific products are selected, filter those
      if (selectedProductIds && selectedProductIds.length > 0) {
        const filteredProducts = {};
        let storeTotalQuantity = 0;
        let storeTotalPrice = 0;

        Object.entries(store.products).forEach(([productId, productData]) => {
          if (selectedProductIds.includes(productId)) {
            filteredProducts[productId] = productData;
            storeTotalQuantity += productData.quantity;
            storeTotalPrice += productData.price * productData.quantity;
            filteredCart.totalItems += productData.quantity;
            filteredCart.totalPrice += productData.price * productData.quantity;
          }
        });

        filteredCart.baskets[storeId] = {
          ...store,
          products: filteredProducts,
          totalQuantity: storeTotalQuantity,
          totalPrice: storeTotalPrice
        };
      } else {
        // Use the entire store
        filteredCart.totalItems += store.totalQuantity;
        filteredCart.totalPrice += store.totalPrice;
      }

      // Prepare checkout data with filtered cart
      const checkoutData = prepareCheckoutData(filteredCart, shippingDetails, paymentDetails);

      // Call appropriate API based on user status
      let response;
      if (isGuest) {
        response = await checkoutGuest(checkoutData);
      } else {
        response = await checkout(username, token, checkoutData);
      }

      // Handle successful checkout
      if (response && !response.error && response.data) {
        const checkoutResult = response.data;

        // Debug: Log the response
        console.log("Store checkout response:", response);
        console.log("Store checkout result:", checkoutResult);

        // FIXED: Handle orderIds as List<String> from CheckoutResultDTO
        let orderId = null;

        if (checkoutResult.orderIds && Array.isArray(checkoutResult.orderIds) && checkoutResult.orderIds.length > 0) {
          orderId = checkoutResult.orderIds[0];

          if (checkoutResult.orderIds.length > 1) {
            console.log("Multiple orders created:", checkoutResult.orderIds);
          }
        }

        if (!orderId) {
          console.error("No order IDs found in store checkout response:", checkoutResult);
          console.error("orderIds field:", checkoutResult.orderIds);
          alert("Checkout completed but no order IDs found. Please check your order history.");
          return;
        }

        // Remove checked out items from cart
        const updatedCart = { ...cart };

        if (selectedProductIds && selectedProductIds.length > 0) {
          // Remove only selected products
          selectedProductIds.forEach(productId => {
            if (updatedCart.baskets[storeId].products[productId]) {
              const product = updatedCart.baskets[storeId].products[productId];
              updatedCart.baskets[storeId].totalQuantity -= product.quantity;
              updatedCart.baskets[storeId].totalPrice -= product.price * product.quantity;
              updatedCart.totalItems -= product.quantity;
              updatedCart.totalPrice -= product.price * product.quantity;
              delete updatedCart.baskets[storeId].products[productId];
            }
          });

          // Remove store if empty
          if (Object.keys(updatedCart.baskets[storeId].products).length === 0) {
            delete updatedCart.baskets[storeId];
          }
        } else {
          // Remove entire store
          updatedCart.totalItems -= store.totalQuantity;
          updatedCart.totalPrice -= store.totalPrice;
          delete updatedCart.baskets[storeId];
        }

        setCart(updatedCart);

        // Update selected products list
        if (selectedProductIds && selectedProductIds.length > 0) {
          setSelectedProducts(prev => prev.filter(id => !selectedProductIds.includes(id)));
        }

        // Update local storage for guest cart if needed
        if (isGuest) {
          const guestCart = { baskets: {} };
          Object.entries(updatedCart.baskets).forEach(([storeId, storeData]) => {
            guestCart.baskets[storeId] = {};
            Object.entries(storeData.products).forEach(([productId, productData]) => {
              guestCart.baskets[storeId][productId] = productData.quantity;
            });
          });
          localStorage.setItem("guestCart", JSON.stringify(guestCart));
        }

        // Show success message
        alert(`Checkout successful! Order ID: ${orderId}`);

        // Redirect to order confirmation page
        window.location.href = `/order-confirmation/${orderId}`;
      } else {
        const errorMessage = response.errorMessage || "Unknown error occurred";
        throw new Error(errorMessage);
      }
    } catch (error) {
      console.error("Store checkout failed:", error);

      let errorMessage = "Unknown error";
      if (error.response?.data?.errorMessage) {
        errorMessage = error.response.data.errorMessage;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      alert(`Store checkout failed: ${errorMessage}`);
    } finally {
      setLoading(false);
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
