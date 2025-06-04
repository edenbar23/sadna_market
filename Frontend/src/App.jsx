import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import ErrorBoundary from "./components/ErrorBoundary";
import { useAuthContext } from './context/AuthContext';
import { CartProvider } from './context/CartContext.jsx';

// Regular Pages
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
import ProfilePage from "./pages/ProfilePage";

// Admin Pages
import AdminDashboardPage from "./pages/AdminDashboardPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import AdminStoresPage from "./pages/AdminStoresPage";
import AdminReportsPage from "./pages/AdminReportsPage";
import AdminInsightsPage from "./pages/AdminInsightsPage";

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
                        {/* Public Routes */}
                        <Route path="/" element={<MainPage />} />
                        <Route path="/cart" element={<CartPage />} />
                        <Route path="/search" element={<SearchResultsPage />} />
                        <Route path="/product/:productId" element={<ProductPage />} />
                        <Route path="/store/:storeId" element={<StorePage />} />
                        <Route path="/order-confirmation/:orderId" element={<OrderConfirmationPage />} />

                        {/* Protected User Routes */}
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
                        <Route
                            path="/my-profile"
                            element={
                                <ProtectedRoute user={user}>
                                    <ProfilePage />
                                </ProtectedRoute>
                            }
                        />
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

                        {/* Protected Admin Routes */}
                        <Route
                            path="/admin"
                            element={
                                <ProtectedRoute user={user} requireAdmin={true}>
                                    <AdminDashboardPage />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/admin/dashboard"
                            element={
                                <ProtectedRoute user={user} requireAdmin={true}>
                                    <AdminDashboardPage />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/admin/users"
                            element={
                                <ProtectedRoute user={user} requireAdmin={true}>
                                    <AdminUsersPage />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/admin/stores"
                            element={
                                <ProtectedRoute user={user} requireAdmin={true}>
                                    <AdminStoresPage />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/admin/reports"
                            element={
                                <ProtectedRoute user={user} requireAdmin={true}>
                                    <AdminReportsPage />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/admin/insights"
                            element={
                                <ProtectedRoute user={user} requireAdmin={true}>
                                    <AdminInsightsPage />
                                </ProtectedRoute>
                            }
                        />

                        {/* 404 Route */}
                        <Route path="*" element={<div>404 Not Found</div>} />
                    </Routes>
                </ErrorBoundary>
            </Router>
        </CartProvider>
    );
}

export default App;