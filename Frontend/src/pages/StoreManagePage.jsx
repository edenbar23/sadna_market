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
        cancelled: false,
        delivered: false
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

            await handleAddProduct({
                ...productData,
                productId: null // Ensure we don't send undefined as productId
            }, quantity);

            // Refresh products list
            await loadStoreData();
            setShowAddProductModal(false);
        } catch (err) {
            console.error("Failed to add product:", err);
            // Handle error
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

            await handleUpdateProduct({
                ...productData,
                productId: productData.id // Ensure we set the correct productId field
            }, productData.quantity);

            // Refresh products list
            await loadStoreData();
            setShowEditProductModal(false);
            setSelectedProduct(null);
        } catch (err) {
            console.error("Failed to update product:", err);
            // Handle error
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
        return <div className="store-manage-container">Loading store data...</div>;
    }

    if (error) {
        return (
            <div className="store-manage-container">
                <div className="error-message">Error: {error}</div>
                <button onClick={() => navigate("/my-stores")} className="btn">
                    Back to My Stores
                </button>
            </div>
        );
    }

    if (!store) {
        return (
            <div className="store-manage-container">
                <div className="error-message">Store not found</div>
                <button onClick={() => navigate("/my-stores")} className="btn">
                    Back to My Stores
                </button>
            </div>
        );
    }

    // Get personnel list from store data
    const personnel = getPersonnel();

    return (
        <div className="store-manage-container">
            <div className="store-header">
                <h1 className="store-title">{store.name}</h1>
                <span className={`store-status ${store.active ? "active" : "inactive"}`}>
                    {store.active ? "Active" : "Closed"}
                </span>
            </div>

            <div className="store-actions">
                <button className="btn" onClick={() => {
                    setAppointmentType("owner");
                    setShowAppointUserModal(true);
                }}>
                    Appoint Owner
                </button>
                <button className="btn" onClick={() => {
                    setAppointmentType("manager");
                    setShowAppointUserModal(true);
                }}>
                    Appoint Manager
                </button>
                <button className="btn" onClick={handleStatusToggle}>
                    {store.active ? "Close Store" : "Activate Store"}
                </button>
                <button className="btn" onClick={() => navigate("/my-stores")}>
                    Back to My Stores
                </button>
            </div>

            <section className="store-section">
                <h2 className="store-section-title">Store Crew</h2>
                {personnel && personnel.length > 0 ? (
                    <div className="personnel-list">
                        {personnel.map((person) => (
                            <div key={person.username} className="personnel-card">
                                <div className="personnel-info">
                                    <span className="personnel-name">{person.username}</span>
                                    <span className="personnel-role">{person.role}</span>
                                </div>
                                <button
                                    className="personnel-remove"
                                    onClick={() => handleRemoveUser(person.username, person.role === "owner")}
                                >
                                    Remove
                                </button>
                            </div>
                        ))}
                    </div>
                ) : (
                    <p className="text-muted">No crew members assigned yet.</p>
                )}
            </section>

            <div className="store-section">
                <div className="section-header">
                    <h2 className="store-section-title">Manage Products</h2>
                    <button
                        className="btn add-product-btn"
                        onClick={() => setShowAddProductModal(true)}
                    >
                        Add Product
                    </button>
                </div>
                <div className="products-scroll">
                    {products.length > 0 ? (
                        products.map((product) => (
                            <div key={product.productId} className="product-card">
                                <div className="product-info">
                                    <h3 className="product-name">{product.name}</h3>
                                    <p className="product-price">Price: ${product.price}</p>
                                    <p className="product-stock">Stock: {product.quantity || 'N/A'}</p>
                                </div>
                                <div className="product-actions">
                                    <button
                                        className="product-edit"
                                        onClick={() => {
                                            setSelectedProduct(product);
                                            setShowEditProductModal(true);
                                        }}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        className="product-delete"
                                        onClick={() => handleDeleteProductConfirm(product.productId)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))
                    ) : (
                        <p className="text-muted">No products in this store yet.</p>
                    )}
                </div>
            </div>

            <section className="store-section">
                <h2 className="store-section-title">Store Messages</h2>
                <StoreMessagesList storeId={storeId} />
            </section>

            <section className="store-section">
                <h2 className="store-section-title">Store Orders</h2>

                {/* Debug info */}
                <div className="debug-info" style={{ backgroundColor: '#f8f9fa', padding: '10px', borderRadius: '4px', marginBottom: '20px', fontSize: '12px' }}>
                    <p>Store ID: {storeId}</p>
                    <p>Orders loaded: {orders ? orders.length : 'null'}</p>
                    <p>Loading: {isLoading ? 'Yes' : 'No'}</p>
                    {error && <p>Error: {error}</p>}
                </div>

                {isLoading ? (
                    <div className="loading-indicator">Loading orders...</div>
                ) : orders && orders.length > 0 ? (
                    <div className="orders-container">
                        {/* All Orders */}
                        <div className="order-subsection">
                            <div
                                className="order-subsection-header"
                                onClick={() => toggleOrderSection('all')}
                                style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', marginBottom: openOrderSections.all ? '0' : '10px', backgroundColor: '#f8f9fa' }}
                            >
                                <h3>All Orders ({orders.length})</h3>
                                <span style={{ fontSize: '18px', fontWeight: 'bold' }}>
                                    {openOrderSections.all ? '▼' : '▶'}
                                </span>
                            </div>

                            {openOrderSections.all && (
                                <div className="orders-scroll" style={{ border: '1px solid #ddd', borderTop: 'none', borderRadius: '0 0 4px 4px', maxHeight: '400px', overflowY: 'auto', padding: '10px' }}>
                                    {orders.map(order => (
                                        <div key={order.orderId} className="order-card" style={{ border: '1px solid #eee', borderRadius: '4px', padding: '15px', marginBottom: '10px', backgroundColor: '#fff' }}>
                                            <div className="order-header" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px', fontSize: '12px', color: '#666' }}>
                                                <span>Order: {order.orderId}</span>
                                                <span>{formatOrderDate(order.orderDate)}</span>
                                                <span>Buyer: {order.userName}</span>
                                            </div>

                                            <div className="order-products" style={{ marginBottom: '10px' }}>
                                                <h4 style={{ fontSize: '14px', marginBottom: '5px' }}>Products:</h4>
                                                {order.products && Object.keys(order.products).length > 0 ? (
                                                    Object.entries(order.products).map(([productId, quantity]) => (
                                                        <div key={productId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', padding: '2px 0' }}>
                                                            <span>{getProductName(productId)}</span>
                                                            <span>Qty: {quantity}</span>
                                                        </div>
                                                    ))
                                                ) : (
                                                    <p style={{ fontSize: '12px', color: '#888' }}>No products found</p>
                                                )}
                                            </div>

                                            <div className="order-footer" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '12px' }}>
                                                <div>
                                                    <span style={{ marginRight: '15px' }}>Total: ${order.totalPrice?.toFixed(2) || '0.00'}</span>
                                                    <span style={{ marginRight: '15px' }}>Final: ${order.finalPrice?.toFixed(2) || '0.00'}</span>
                                                </div>
                                                <span style={{
                                                    padding: '2px 8px',
                                                    borderRadius: '12px',
                                                    backgroundColor: order.status === 'PAID' ? '#d4edda' : order.status === 'PENDING' ? '#fff3cd' : '#f8d7da',
                                                    color: order.status === 'PAID' ? '#155724' : order.status === 'PENDING' ? '#856404' : '#721c24'
                                                }}>
                                                    {order.status || 'Unknown'}
                                                </span>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Pending Orders */}
                        <div className="order-subsection">
                            <div
                                className="order-subsection-header"
                                onClick={() => toggleOrderSection('pending')}
                                style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', marginBottom: openOrderSections.pending ? '0' : '10px', backgroundColor: '#fff3cd' }}
                            >
                                <h3>Pending Orders ({orders.filter(o => o.status === "PENDING").length})</h3>
                                <span style={{ fontSize: '18px', fontWeight: 'bold' }}>
                                    {openOrderSections.pending ? '▼' : '▶'}
                                </span>
                            </div>

                            {openOrderSections.pending && (
                                <div className="orders-scroll" style={{ border: '1px solid #ddd', borderTop: 'none', borderRadius: '0 0 4px 4px', maxHeight: '400px', overflowY: 'auto', padding: '10px' }}>
                                    {orders.filter(o => o.status === "PENDING").length > 0 ? (
                                        orders.filter(o => o.status === "PENDING").map(order => (
                                            <div key={order.orderId} className="order-card" style={{ border: '1px solid #eee', borderRadius: '4px', padding: '15px', marginBottom: '10px', backgroundColor: '#fff' }}>
                                                <div className="order-header" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px', fontSize: '12px', color: '#666' }}>
                                                    <span>{formatOrderDate(order.orderDate)}</span>
                                                    <span>Buyer: {order.userName}</span>
                                                </div>
                                                <div className="order-footer" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                    <span>Total: ${order.finalPrice?.toFixed(2) || '0.00'}</span>
                                                    <div style={{ display: 'flex', gap: '10px' }}>
                                                        <button style={{ padding: '5px 15px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                                                            Approve
                                                        </button>
                                                        <button style={{ padding: '5px 15px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                                                            Decline
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        ))
                                    ) : (
                                        <p style={{ textAlign: 'center', color: '#888', padding: '20px' }}>No pending orders.</p>
                                    )}
                                </div>
                            )}
                        </div>

                        {/* Paid Orders */}
                        <div className="order-subsection">
                            <div
                                className="order-subsection-header"
                                onClick={() => toggleOrderSection('paid')}
                                style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', marginBottom: openOrderSections.paid ? '0' : '10px', backgroundColor: '#d4edda' }}
                            >
                                <h3>Paid Orders ({orders.filter(o => o.status === "PAID").length})</h3>
                                <span style={{ fontSize: '18px', fontWeight: 'bold' }}>
                                    {openOrderSections.paid ? '▼' : '▶'}
                                </span>
                            </div>

                            {openOrderSections.paid && (
                                <div className="orders-scroll" style={{ border: '1px solid #ddd', borderTop: 'none', borderRadius: '0 0 4px 4px', maxHeight: '400px', overflowY: 'auto', padding: '10px' }}>
                                    {orders.filter(o => o.status === "PAID").length > 0 ? (
                                        orders.filter(o => o.status === "PAID").map(order => (
                                            <div key={order.orderId} className="order-card" style={{ border: '1px solid #eee', borderRadius: '4px', padding: '15px', marginBottom: '10px', backgroundColor: '#fff' }}>
                                                <div className="order-header" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px', fontSize: '12px', color: '#666' }}>
                                                    <span>{formatOrderDate(order.orderDate)}</span>
                                                    <span>Buyer: {order.userName}</span>
                                                </div>
                                                <div className="order-footer" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                    <span>Total: ${order.finalPrice?.toFixed(2) || '0.00'}</span>
                                                    <span style={{ color: '#155724', fontWeight: 'bold' }}>PAID</span>
                                                </div>
                                            </div>
                                        ))
                                    ) : (
                                        <p style={{ textAlign: 'center', color: '#888', padding: '20px' }}>No paid orders.</p>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                ) : (
                    <div className="no-data-message">
                        <p>No orders found for this store.</p>
                        <button onClick={loadStoreData} className="btn">Refresh Orders</button>
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
                    type={appointmentType}
                    onClose={() => setShowAppointUserModal(false)}
                    onSubmit={handleAppointUser}
                />
            )}
        </div>
    );
}