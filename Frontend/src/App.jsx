import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import MainPage from "./pages/MainPage";
import CartPage from "./pages/CartPage";
import HeaderBar from "./components/HeaderBar";

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
      </Routes>
      {!user && <button onClick={fakeLogin}>Fake Login</button>}
    </Router>
  );
}

export default App;
