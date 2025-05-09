import React, { useState } from "react";
import MainPage from "./pages/MainPage";
import HeaderBar from "./components/HeaderBar";

function App() {
  const [user, setUser] = useState(null); // null = guest

  // Simulated login (replace with real logic)
  const fakeLogin = () => setUser({ name: "John", role: "user" });
  const logout = () => setUser(null);

  return (
    <>
      <HeaderBar user={user} onLogout={logout} />
      <MainPage />
      {!user && <button onClick={fakeLogin}>Fake Login</button>}
    </>
  );
}

export default App;
