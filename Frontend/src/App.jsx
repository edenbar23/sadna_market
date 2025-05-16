import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import MainPage from "./pages/MainPage";
import CartPage from "./pages/CartPage";
import OrdersPage from "./pages/OrdersPage";
import MessagesPage from "./pages/MessagesPage";
import SearchResultsPage from "./pages/SearchResultsPage";
import HeaderBar from "./components/HeaderBar";
import ProductPage from "./pages/ProductPage";
import StorePage from "./pages/StorePage";

function App() {
  const [user, setUser] = useState(null); // null = guest

  // Simulated login (replace with real logic)
  const fakeLogin = () => setUser({ name: "John", role: "user" });
  const logout = () => setUser(null);

  return (
    <Router>
      <HeaderBar user={user} onLogout={logout} />
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/messages" element={<MessagesPage />} />
        <Route path="/search" element={<SearchResultsPage />} />
        <Route path="/product/:productId" element={<ProductPage />} />
        <Route path="/store/:storeId" element={<StorePage user={user} />} />
      </Routes>
      {!user && <button onClick={fakeLogin}>Fake Login</button>}
    </Router>
  );
}

export default App;
