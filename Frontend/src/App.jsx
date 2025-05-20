import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import ErrorBoundary from "./components/ErrorBoundary";

import MainPage from "./pages/MainPage";
import CartPage from "./pages/CartPage";
import OrdersPage from "./pages/OrdersPage";
import MessagesPage from "./pages/MessagesPage";
import SearchResultsPage from "./pages/SearchResultsPage";
import HeaderBar from "./components/HeaderBar";
import ProductPage from "./pages/ProductPage";
import StorePage from "./pages/StorePage";
import StoreManagementPage from "./pages/StoreManagementPage";
import StoreManagePage from "./pages/StoreManagePage";

//temporary data for testing
import { mockUsers } from "./data/mockUsers";

function App() {
  const [user, setUser] = useState(null); // null = guest

  // Simulated login (replace with real logic)
  const fakeLogin = () => {
    const alice = mockUsers.find((u) => u.username === "alice123");
    if (alice) setUser(alice);
    else console.error("alice123 not found in mockUsers");
  };

  const fakeAdminLogin = () => {
    const admin = mockUsers.find((u) => u.username === "admin");
    if (admin) setUser(admin);
    else console.error("admin not found in mockUsers");
  };

  const login = (userData) => {
    setUser(userData);
  };

  const logout = () => setUser(null);

  return (
      <Router>
        <ErrorBoundary>
          <HeaderBar user={user} onLogout={logout} onLogin={login} />
          <Routes>
            <Route path="/" element={<MainPage />} />
            <Route path="/cart" element={<CartPage />} />
            <Route
                path="/orders"
                element={
                  <ProtectedRoute user={user}>
                    <OrdersPage />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/messages"
                element={
                  <ProtectedRoute user={user}>
                    <MessagesPage />
                  </ProtectedRoute>
                }
            />
            <Route path="/search" element={<SearchResultsPage />} />
            <Route path="/product/:productId" element={<ProductPage user={user} />} />
            <Route path="/store/:storeId" element={<StorePage user={user} />} />
            <Route
                path="/my-stores"
                element={
                  <ProtectedRoute user={user}>
                    <StoreManagementPage user={user} />
                  </ProtectedRoute>
                }
            />
            <Route
                path="/store-manage/:storeId"
                element={
                  <ProtectedRoute user={user}>
                    <StoreManagePage user={user} />
                  </ProtectedRoute>
                }
            />
            <Route path="*" element={<div>404 Not Found</div>} />
          </Routes>
          {!user && (
              <div className="dev-login-buttons">
                <button onClick={fakeLogin}>Fake Login</button>
                <button onClick={fakeAdminLogin}>Fake Admin Login</button>
              </div>
          )}
        </ErrorBoundary>
      </Router>
  );
}

export default App;