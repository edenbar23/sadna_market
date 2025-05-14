import { useState } from "react";
import axios from "axios";

export default function useRegister() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const register = async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      const res = await axios.post("/api/auth/register", {
        username,
        password,
      });
      return res.data;
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed");
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { register, loading, error };
}
