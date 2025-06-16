import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuthContext } from "../context/AuthContext";
import { useStoreManagement } from "@/hooks/index.js";
import { useProductOperations } from "@/hooks/index.js";
import { useStoreOperations } from "@/hooks/index.js";
import { useStorePersonnel } from "@/hooks/index.js";
import AddProductModal from "../components/AddProductModal";
import EditProductModal from "../components/EditProductModal";
import AppointUserModal from "../components/AppointUserModal";
import StoreMessagesList from "../components/StoreMessagesList";

export default function StoreManagePage() {
    const { storeId } = useParams();
    const navigate = useNavigate();
    const { user } = useAuthContext();

    // Use store management hook to get store data
    const {
        store,
        products,
        orders,
        messages,
        isLoading,
        error,
        loadStoreData,
        addProductToList,
        updateProductInList,
        removeProductFromList
    } = useStoreManagement(storeId, user);

    // Product operations
    const {
        handleAddProduct,
        handleUpdateProduct,
        handleDeleteProduct
    } = useProductOperations(storeId, user);

    // Store operations
    const { handleToggleStoreStatus } = useStoreOperations(user);

    // Personnel operations
    const {
        handleAppointOwner,
        handleRemoveOwner,
        handleAppointManager,
        handleRemoveManager
    } = useStorePersonnel(storeId, user);

    // Local state for modals
    const [showAddProductModal, setShowAddProductModal] = useState(false);
    const [showEditProductModal, setShowEditProductModal] = useState(false);
    const [showAppointUserModal, setShowAppointUserModal] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [appointmentType, setAppointmentType] = useState("owner"); // "owner" or "manager"

    // State for collapsible order sections
    const [openOrderSections, setOpenOrderSections] = useState({
        all: false,
        pending: false,
        paid: false,
        shipped: false,
        cancelled: false,
        completed: false
    });

    // Helper function to format dates
    const formatOrderDate = (dateStr) => {
        if (!dateStr) return 'No date available';

        try {
            let date;
            if (Array.isArray(dateStr)) {
                // If it's an array like [2024, 1, 15, 10, 30, 0], convert to Date
                const [year, month, day, hour = 0, minute = 0, second = 0] = dateStr;
                date = new Date(year, month - 1, day, hour, minute, second);
            } else {
                date = new Date(dateStr);
            }

            if (isNaN(date.getTime())) {
                console.warn("Invalid date:", dateStr);
                return 'Invalid date';
            }

            return date.toLocaleString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                hour12: true
            });
        } catch (error) {
            console.error("Date formatting error:", error, dateStr);
            return 'Date format error';
        }
    };

    const getProductName = (productId) => {
        if (!products || products.length === 0) return `Product ID: ${productId.substring(0, 8)}...`;

        const product = products.find(p => p.productId === productId);
        return product ? product.name : `Unknown Product (${productId.substring(0, 8)}...)`;
    };

    const toggleOrderSection = (sectionName) => {
        setOpenOrderSections(prev => ({
            ...prev,
            [sectionName]: !prev[sectionName]
        }));
    };

    // Transform ownerUsernames and managerUsernames into personnel array
    const getPersonnel = () => {
        if (!store) return [];

        const personnel = [];

        // Add owners to personnel
        if (store.ownerUsernames && Array.isArray(store.ownerUsernames)) {
            store.ownerUsernames.forEach(username => {
                personnel.push({ username, role: "owner" });
            });
        }

        // Add managers to personnel
        if (store.managerUsernames && Array.isArray(store.managerUsernames)) {
            store.managerUsernames.forEach(username => {
                personnel.push({ username, role: "manager" });
            });
        }

        return personnel;
    };

    // Handle adding a new product
    const handleAddProductSubmit = async (productData, quantity) => {
        try {
            // Make sure we have a valid storeId
            if (!storeId || storeId === "undefined") {
                throw new Error("Invalid store ID");
            }

            const result = await handleAddProduct({
                ...productData,
                productId: null // Ensure we don't send undefined as productId
            }, quantity);

            if (result && result.data) {
                // Refresh products list
                await loadStoreData();
                setShowAddProductModal(false);
            } else {
                throw new Error("Failed to add product: No response from server");
            }
        } catch (err) {
            console.error("Failed to add product:", err);
            alert(err.message || "Failed to add product. Please try again.");
        }
    };

    // Handle editing an existing product
    const handleEditProductSubmit = async (productData) => {
        try {
            // Make sure we have valid IDs
            if (!storeId || storeId === "undefined") {
                throw new Error("Invalid store ID");
            }
            if (!productData.id || productData.id === "undefined") {
                throw new Error("Invalid product ID");
            }

            const formattedData = {
                productId: productData.id,
                name: productData.name,
                description: productData.description || "",
                category: productData.category || "",
                price: parseFloat(productData.price),
                quantity: parseInt(productData.quantity) || 1
            };

            const result = await handleUpdateProduct(formattedData, formattedData.quantity);

            if (result && result.data) {
                // Refresh products list
                await loadStoreData();
                setShowEditProductModal(false);
                setSelectedProduct(null);
            } else {
                throw new Error("Failed to update product: No response from server");
            }
        } catch (err) {
            console.error("Failed to update product:", err);
            alert(err.message || "Failed to update product. Please try again.");
        }
    };

    // Handle deleting a product
    const handleDeleteProductConfirm = async (productId) => {
        try {
            await handleDeleteProduct(productId);
            removeProductFromList(productId); // Update UI
        } catch (err) {
            console.error("Failed to delete product:", err);
        }
    };

    // Handle store status toggle
    const handleStatusToggle = async () => {
        try {
            await handleToggleStoreStatus(storeId, store.active);
            // Reload store data to get updated status
            loadStoreData();
        } catch (err) {
            console.error("Failed to toggle store status:", err);
        }
    };

    // Handle appointing store personnel
    const handleAppointUser = async (username, permissions = []) => {
        try {
            if (appointmentType === "owner") {
                await handleAppointOwner(username);
            } else {
                await handleAppointManager(username, permissions);
            }

            // Close modal and reload data only after successful appointment
            setShowAppointUserModal(false);
            loadStoreData();

            // Success feedback
            alert(`${appointmentType === "owner" ? "Owner" : "Manager"} appointed successfully!`);

        } catch (err) {
            console.error(`Failed to appoint ${appointmentType}:`, err);
            // Don't close modal on error, let user try again
            throw err; // Re-throw so modal can handle the error
        }
    };

    // Handle removing store personnel
    const handleRemoveUser = async (username, isOwner) => {
        try {
            if (isOwner) {
                await handleRemoveOwner(username);
            } else {
                await handleRemoveManager(username);
            }
            // Reload store data to get updated personnel
            loadStoreData();
        } catch (err) {
            console.error(`Failed to remove ${isOwner ? 'owner' : 'manager'}:`, err);
        }
    };

    if (isLoading) {
        return (
            <div className="store-manage-container">
                <div className="loading-indicator">Loading store data...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="store-manage-container">
                <div className="error-message">Error: {error}</div>
                <button onClick={() => navigate("/my-stores")} className="store-manage-btn">
                    Back to My Stores
                </button>
            </div>
        );
    }

    if (!store) {
        return (
            <div className="store-manage-container">
                <div className="error-message">Store not found</div>
                <button onClick={() => navigate("/my-stores")} className="store-manage-btn">
                    Back to My Stores
                </button>
            </div>
        );
    }

    // Get personnel list from store data
    const personnel = getPersonnel();

    return (
        <div className="store-manage-container">
            <div className="store-manage-header">
                <h1 className="store-manage-title">{store.name}</h1>
                <span className={`store-manage-status ${store.active ? "active" : "inactive"}`}>
                    {store.active ? "Active" : "Closed"}
                </span>
            </div>

            <div className="store-manage-actions">
                <button className="store-manage-btn" onClick={() => {
                    setAppointmentType("owner");
                    setShowAppointUserModal(true);
                }}>
                    Appoint Owner
                </button>
                <button className="store-manage-btn" onClick={() => {
                    setAppointmentType("manager");
                    setShowAppointUserModal(true);
                }}>
                    Appoint Manager
                </button>
                <button className="store-manage-btn secondary" onClick={handleStatusToggle}>
                    {store.active ? "Close Store" : "Reopen Store"}
                </button>
                <button className="store-manage-btn" onClick={() => setShowAddProductModal(true)}>
                    Add Product
                </button>
            </div>

            {/* Store Personnel Section */}
            <section className="store-manage-section">
                <h2 className="store-manage-section-title">Store Personnel</h2>

                {personnel.length > 0 ? (
                    <div className="personnel-list">
                        {personnel.map((person) => (
                            <div key={`${person.role}-${person.username}`} className="personnel-item">
                                <div className="personnel-info">
                                    <div className="personnel-username">@{person.username}</div>
                                    <div className={`personnel-role ${person.role}`}>{person.role}</div>
                                </div>
                                <div className="personnel-actions">
                                    <button
                                        className="personnel-remove-btn"
                                        onClick={() => handleRemoveUser(person.username, person.role === "owner")}
                                    >
                                        Remove
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="empty-state">
                        <div className="empty-state-icon">👥</div>
                        <div className="empty-state-text">No personnel assigned</div>
                        <div className="empty-state-subtext">Use the buttons above to appoint owners or managers</div>
                    </div>
                )}
            </section>

            {/* Store Products Section */}
            <section className="store-manage-section">
                <h2 className="store-manage-section-title">Store Products</h2>

                {products && products.length > 0 ? (
                    <div className="products-grid">
                        {products.map((product) => (
                            <div key={product.productId} className="product-manage-card">
                                <div className="product-manage-info">
                                    <h3 className="product-manage-name">{product.name}</h3>
                                    <p className="product-manage-price">Price: ${product.price}</p>

                                    {/* ✅ Now shows actual available stock */}
                                    <p className="product-manage-stock">
                                        Stock: {product.quantity !== undefined ? product.quantity : 'N/A'}
                                    </p>

                                    {/* ✅ Visual stock indicators */}
                                    {product.quantity === 0 && (
                                        <p className="product-manage-out-of-stock" style={{color: 'red', fontSize: '0.8rem'}}>
                                            Out of Stock
                                        </p>
                                    )}
                                    {product.quantity > 0 && product.quantity <= 5 && (
                                        <p className="product-manage-low-stock" style={{color: 'orange', fontSize: '0.8rem'}}>
                                            Low Stock
                                        </p>
                                    )}
                                    {product.quantity > 5 && (
                                        <p className="product-manage-in-stock" style={{color: 'green', fontSize: '0.8rem'}}>
                                            In Stock
                                        </p>
                                    )}
                                </div>
                                <div className="product-manage-actions">
                                    <button
                                        className="product-edit-btn"
                                        onClick={() => {
                                            setSelectedProduct(product);
                                            setShowEditProductModal(true);
                                        }}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        className="product-delete-btn"
                                        onClick={() => handleDeleteProductConfirm(product.productId)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="empty-state">
                        <div className="empty-state-icon">📦</div>
                        <div className="empty-state-text">No products in this store yet</div>
                        <div className="empty-state-subtext">Add your first product to get started</div>
                    </div>
                )}
            </section>

            {/* Store Messages Section */}
            <section className="store-manage-section">
                <h2 className="store-manage-section-title">Store Messages</h2>
                <div className="store-messages-container">
                    <StoreMessagesList storeId={storeId} />
                </div>
            </section>

            {/* Store Orders Section */}
            <section className="store-manage-section">
                <h2 className="store-manage-section-title">Store Orders</h2>

                {/* Debug info */}
                <div className="debug-info">
                    <p><strong>Store Name:</strong> {store.name}</p>
                    <p><strong>Orders loaded:</strong> {orders ? orders.length : 'null'}</p>
                    <p><strong>Loading:</strong> {isLoading ? 'Yes' : 'No'}</p>
                    {error && <p><strong>Error:</strong> {error}</p>}
                </div>

                {isLoading ? (
                    <div className="loading-indicator">Loading orders...</div>
                ) : orders && orders.length > 0 ? (
                    <div className="orders-section">
                        {/* All Orders */}
                        <div className="orders-category">
                            <div
                                className="orders-category-header"
                                onClick={() => toggleOrderSection('all')}
                            >
                                <h3 className="orders-category-title">All Orders ({orders.length})</h3>
                                <span className={`orders-expand-icon ${openOrderSections.all ? 'open' : ''}`}>
                                    ▶
                                </span>
                            </div>
                            {openOrderSections.all && (
                                <div className="orders-list">
                                    {orders.map((order) => (
                                        <div key={order.orderId} className="order-manage-item">
                                            <div className="order-manage-info">
                                                <div className="order-manage-id">Order #{order.orderId?.substring(0, 8)}...</div>
                                                <div className="order-manage-date">{formatOrderDate(order.orderDate)}</div>
                                                <div className="order-manage-total">Total: ${order.totalCost}</div>
                                            </div>
                                            <span className={`order-manage-status ${order.status?.toLowerCase()}`}>
                                                {order.status}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Pending Orders */}
                        <div className="orders-category">
                            <div
                                className="orders-category-header pending"
                                onClick={() => toggleOrderSection('pending')}
                            >
                                <h3 className="orders-category-title">Pending Orders ({orders.filter(o => o.status === "PENDING").length})</h3>
                                <span className={`orders-expand-icon ${openOrderSections.pending ? 'open' : ''}`}>
                                    ▶
                                </span>
                            </div>
                            {openOrderSections.pending && (
                                <div className="orders-list">
                                    {orders.filter(o => o.status === "PENDING").length > 0 ? (
                                        orders.filter(o => o.status === "PENDING").map((order) => (
                                            <div key={order.orderId} className="order-manage-item">
                                                <div className="order-manage-info">
                                                    <div className="order-manage-id">Order #{order.orderId?.substring(0, 8)}...</div>
                                                    <div className="order-manage-date">{formatOrderDate(order.orderDate)}</div>
                                                    <div className="order-manage-total">Total: ${order.totalCost}</div>
                                                </div>
                                                <span className="order-manage-status pending">
                                                    {order.status}
                                                </span>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="empty-state">
                                            <div className="empty-state-text">No pending orders</div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* Paid Orders */}
                        <div className="orders-category">
                            <div
                                className="orders-category-header paid"
                                onClick={() => toggleOrderSection('paid')}
                            >
                                <h3 className="orders-category-title">Paid Orders ({orders.filter(o => o.status === "PAID").length})</h3>
                                <span className={`orders-expand-icon ${openOrderSections.paid ? 'open' : ''}`}>
                                    ▶
                                </span>
                            </div>
                            {openOrderSections.paid && (
                                <div className="orders-list">
                                    {orders.filter(o => o.status === "PAID").length > 0 ? (
                                        orders.filter(o => o.status === "PAID").map((order) => (
                                            <div key={order.orderId} className="order-manage-item">
                                                <div className="order-manage-info">
                                                    <div className="order-manage-id">Order #{order.orderId?.substring(0, 8)}...</div>
                                                    <div className="order-manage-date">{formatOrderDate(order.orderDate)}</div>
                                                    <div className="order-manage-total">Total: ${order.totalCost}</div>
                                                </div>
                                                <span className="order-manage-status paid">
                                                    {order.status}
                                                </span>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="empty-state">
                                            <div className="empty-state-text">No paid orders</div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                        {/* Shipped Orders */}
                        <div className="orders-category">
                            <div
                                className="orders-category-header shipped"
                                onClick={() => toggleOrderSection('shipped')}
                            >
                                <h3 className="orders-category-title">Shipped Orders ({orders.filter(o => o.status === "SHIPPED").length})</h3>
                                <span className={`orders-expand-icon ${openOrderSections.shipped ? 'open' : ''}`}>
                        ▶
                    </span>
                            </div>
                            {openOrderSections.shipped && (
                                <div className="orders-list">
                                    {orders.filter(o => o.status === "SHIPPED").length > 0 ? (
                                        orders.filter(o => o.status === "SHIPPED").map((order) => (
                                            <div key={order.orderId} className="order-manage-item">
                                                <div className="order-manage-info">
                                                    <div className="order-manage-id">Order #{order.orderId?.substring(0, 8)}...</div>
                                                    <div className="order-manage-date">{formatOrderDate(order.orderDate)}</div>
                                                    <div className="order-manage-total">Total: ${order.totalCost}</div>
                                                </div>
                                                <span className="order-manage-status shipped">
                                        {order.status}
                                    </span>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="empty-state">
                                            <div className="empty-state-text">No shipped orders</div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* Cancelled Orders */}
                        <div className="orders-category">
                            <div
                                className="orders-category-header cancelled"
                                onClick={() => toggleOrderSection('cancelled')}
                            >
                                <h3 className="orders-category-title">Cancelled Orders ({orders.filter(o => o.status === "CANCELLED").length})</h3>
                                <span className={`orders-expand-icon ${openOrderSections.cancelled ? 'open' : ''}`}>
                                    ▶
                                </span>
                            </div>
                            {openOrderSections.cancelled && (
                                <div className="orders-list">
                                    {orders.filter(o => o.status === "CANCELLED").length > 0 ? (
                                        orders.filter(o => o.status === "CANCELLED").map((order) => (
                                            <div key={order.orderId} className="order-manage-item">
                                                <div className="order-manage-info">
                                                    <div className="order-manage-id">Order #{order.orderId?.substring(0, 8)}...</div>
                                                    <div className="order-manage-date">{formatOrderDate(order.orderDate)}</div>
                                                    <div className="order-manage-total">Total: ${order.totalCost}</div>
                                                </div>
                                                <span className="order-manage-status cancelled">
                                                    {order.status}
                                                </span>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="empty-state">
                                            <div className="empty-state-text">No cancelled orders</div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* Completed Orders */}
                        <div className="orders-category">
                            <div
                                className="orders-category-header completed"
                                onClick={() => toggleOrderSection('completed')}
                            >
                                <h3 className="orders-category-title">Completed Orders ({orders.filter(o => o.status === "COMPLETED").length})</h3>
                                <span className={`orders-expand-icon ${openOrderSections.completed ? 'open' : ''}`}>
                                    ▶
                                </span>
                            </div>
                            {openOrderSections.completed && (
                                <div className="orders-list">
                                    {orders.filter(o => o.status === "COMPLETED").length > 0 ? (
                                        orders.filter(o => o.status === "COMPLETED").map((order) => (
                                            <div key={order.orderId} className="order-manage-item">
                                                <div className="order-manage-info">
                                                    <div className="order-manage-id">Order #{order.orderId?.substring(0, 8)}...</div>
                                                    <div className="order-manage-date">{formatOrderDate(order.orderDate)}</div>
                                                    <div className="order-manage-total">Total: ${order.totalCost}</div>
                                                </div>
                                                <span className="order-manage-status completed">
                                                    {order.status}
                                                </span>
                                            </div>
                                        ))
                                    ) : (
                                        <div className="empty-state">
                                            <div className="empty-state-text">No completed orders</div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                ) : (
                    <div className="empty-state">
                        <div className="empty-state-icon">📋</div>
                        <div className="empty-state-text">No orders yet</div>
                        <div className="empty-state-subtext">Orders will appear here when customers make purchases</div>
                    </div>
                )}
            </section>

            {/* Modals */}
            {showAddProductModal && (
                <AddProductModal
                    onClose={() => setShowAddProductModal(false)}
                    onSubmit={handleAddProductSubmit}
                />
            )}

            {showEditProductModal && selectedProduct && (
                <EditProductModal
                    product={selectedProduct}
                    onClose={() => {
                        setShowEditProductModal(false);
                        setSelectedProduct(null);
                    }}
                    onSubmit={handleEditProductSubmit}
                />
            )}

            {showAppointUserModal && (
                <AppointUserModal
                    appointmentType={appointmentType}
                    onClose={() => setShowAppointUserModal(false)}
                    onSubmit={handleAppointUser}
                />
            )}
        </div>
    );
}