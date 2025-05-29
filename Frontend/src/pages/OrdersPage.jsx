import OrderCard from "../components/OrderCard";
import { useState, useEffect } from "react";
import { fetchOrderHistory } from "../api/order"; 
import { useAuthContext } from "../context/AuthContext";
import "../index.css";

// Dummy orders data
// const orders = [
//   {
//     id: 1,
//     storeName: "Tech Store",
//     products: [
//       { name: "Laptop", quantity: 1 },
//       { name: "Mouse", quantity: 2 },
//     ],
//     paymentMethod: "Credit Card",
//     deliveryAddress: "123 Main St, Tel Aviv",
//     totalPrice: 1250,
//     status: "Shipped",
//   },
//   {
//     id: 2,
//     storeName: "Book Shop",
//     products: [
//       { name: "React Handbook", quantity: 1 },
//     ],
//     paymentMethod: "PayPal",
//     deliveryAddress: "45 Rothschild Blvd, Tel Aviv",
//     totalPrice: 30,
//     status: "Processing",
//   },
// ];

export default function OrdersPage() {
const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const { user, token } = useAuthContext();

  useEffect(() => {
    const fetchOrders = async () => {
      if (!user?.username || !token) {
        console.error("Username or token not found in AuthContext");
        return;
      }

      try {
        const response = await fetchOrderHistory(user.username, token);
        setOrders(response.data); // Assuming API returns orders array
      } catch (error) {
        console.error("Failed to fetch orders:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [user, token]);


    const handleMessageStore = (storeName) => {
        alert(`Messaging ${storeName}... (simulate messaging logic here)`);
  };

  if (loading) {
    return <div className="orders-page"><h1 className="orders-title">Loading orders...</h1></div>;
  }

  if (!orders.length) {
    return <div className="orders-page"><h1 className="orders-title">No orders found.</h1></div>;
  }

  return (
    <div className="orders-page">
    <h1 className="orders-title">My Orders</h1>
    <div className="orders-list">
      {orders.map((order) => (
        <div key={order.id} className="order-card">
          <OrderCard order={order} />
          <button
            className="message-store-btn"
            onClick={() => handleMessageStore(order.storeName)}
          >
            Message {order.storeName}
          </button>
        </div>
      ))}
    </div>
  </div>
  );
}
