import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import ErrorBoundary from "./components/ErrorBoundary";
import { useAuthContext } from './context/AuthContext';
import { CartProvider } from './context/CartContext.jsx';

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
import OrderConfirmationPage from "./pages/OrderConfirmationPage";

function App() {
    const { user, loading } = useAuthContext();

    // Show loading while validating authentication
    if (loading) {
        return (
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '100vh',
                fontSize: '18px'
            }}>
                Validating session...
            </div>
        );
    }

    return (
        <CartProvider>
            <Router>
                <ErrorBoundary>
                    <HeaderBar />
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
                        <Route path="/product/:productId" element={<ProductPage />} />
                        <Route path="/store/:storeId" element={<StorePage />} />
                        <Route
                            path="/my-stores"
                            element={
                                <ProtectedRoute user={user}>
                                    <StoreManagementPage />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/store-manage/:storeId"
                            element={
                                <ProtectedRoute user={user}>
                                    <StoreManagePage />
                                </ProtectedRoute>
                            }
                        />
                        <Route path="*" element={<div>404 Not Found</div>} />
                    </Routes>
                </ErrorBoundary>
            </Router>
        </CartProvider>
    );
}

export default App;