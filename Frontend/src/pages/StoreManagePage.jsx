import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useStoreManagement } from "@/hooks/index.js";
import { useProductOperations } from "@/hooks/index.js";
import { useStoreOperations } from "@/hooks/index.js";
import { useStorePersonnel } from "@/hooks/index.js";
import AddProductModal from "../components/AddProductModal";
import EditProductModal from "../components/EditProductModal";
import AppointUserModal from "../components/AppointUserModal";
import StoreMessagesList from "../components/StoreMessagesList";

export default function StoreManagePage({ user }) {
    const { storeId } = useParams();
    const navigate = useNavigate();

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
            setShowAppointUserModal(false);
            // Reload store data to get updated personnel
            loadStoreData();
        } catch (err) {
            console.error(`Failed to appoint ${appointmentType}:`, err);
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
                {store.personnel && store.personnel.length > 0 ? (
                    <div className="personnel-list">
                        {store.personnel.map((person) => (
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

                {/* Pending Orders */}
                <div className="order-subsection">
                    <h3 className="order-subsection-title">Pending Orders</h3>
                    <div className="orders-scroll">
                        {orders.filter(o => o.status === "Pending").length > 0 ? (
                            orders.filter(o => o.status === "Pending").map(order => (
                                <div key={order.id} className="order-card">
                                    <div className="order-header">
                                        <span className="order-date">{order.orderedAt}</span>
                                        <span className="order-buyer">Buyer: {order.buyer}</span>
                                    </div>
                                    <ul className="order-items">
                                        {order.items.map((item, idx) => (
                                            <li key={idx}>
                                                {item.product} - Qty: {item.quantity}, ${item.price}
                                            </li>
                                        ))}
                                    </ul>
                                    <div className="order-footer">
                                        <span className="order-total">Total: ${order.total.toFixed(2)}</span>
                                        <div className="order-actions">
                                            <button className="btn-approve">Approve</button>
                                            <button className="btn-decline">Decline</button>
                                        </div>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p className="text-muted">No pending orders.</p>
                        )}
                    </div>
                </div>

                {/* In Process Orders */}
                <div className="order-subsection">
                    <h3 className="order-subsection-title">In Process</h3>
                    <div className="orders-scroll">
                        {orders.filter(o => o.status !== "Complete" && o.status !== "Pending").length > 0 ? (
                            orders.filter(o => o.status !== "Complete" && o.status !== "Pending").map(order => (
                                <div key={order.id} className="order-card">
                                    <div className="order-header">
                                        <span className="order-date">{order.orderedAt}</span>
                                        <span className="order-buyer">Buyer: {order.buyer}</span>
                                    </div>
                                    <ul className="order-items">
                                        {order.items.map((item, idx) => (
                                            <li key={idx}>
                                                {item.product} - Qty: {item.quantity}, ${item.price}
                                            </li>
                                        ))}
                                    </ul>
                                    <div className="order-footer">
                                        <span className="order-total">Total: ${order.total.toFixed(2)}</span>
                                        <span className="order-status">Status: In Process</span>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p className="text-muted">No in-process orders.</p>
                        )}
                    </div>
                </div>

                {/* Completed Orders */}
                <div className="order-subsection">
                    <h3 className="order-subsection-title">Completed</h3>
                    <div className="orders-scroll">
                        {orders.filter(o => o.status === "Complete").length > 0 ? (
                            orders.filter(o => o.status === "Complete").map(order => (
                                <div key={order.id} className="order-card">
                                    <div className="order-header">
                                        <span className="order-date">{order.orderedAt}</span>
                                        <span className="order-buyer">Buyer: {order.buyer}</span>
                                    </div>
                                    <ul className="order-items">
                                        {order.items.map((item, idx) => (
                                            <li key={idx}>
                                                {item.product} - Qty: {item.quantity}, ${item.price}
                                            </li>
                                        ))}
                                    </ul>
                                    <div className="order-footer">
                                        <span className="order-total">Total: ${order.total.toFixed(2)}</span>
                                        <span className="order-status">Status: Completed</span>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p className="text-muted">No completed orders.</p>
                        )}
                    </div>
                </div>
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