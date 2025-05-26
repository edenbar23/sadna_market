import React, { useState } from "react";
import "../index.css";


const PERMISSION_OPTIONS = [
    { id: "MANAGE_INVENTORY", label: "Manage Inventory", description: "Add, update, and remove products" },
    { id: "ADD_PRODUCT", label: "Add Products", description: "Add new products to the store" },
    { id: "REMOVE_PRODUCT", label: "Remove Products", description: "Remove products from the store" },
    { id: "UPDATE_PRODUCT", label: "Update Products", description: "Edit existing product details" },
    { id: "MANAGE_PURCHASE_POLICY", label: "Manage Purchase Policy", description: "Set purchase rules and restrictions" },
    { id: "MANAGE_DISCOUNT_POLICY", label: "Manage Discount Policy", description: "Create and manage discount rules" },
    { id: "RESPOND_TO_USER_INQUIRIES", label: "Reply to Messages", description: "Respond to customer messages" },
    { id: "VIEW_STORE_PURCHASE_HISTORY", label: "View Purchase History", description: "Access store sales history" },
    { id: "RESPOND_TO_BID", label: "Respond to Bids", description: "Handle customer bids on products" },
    { id: "MANAGE_AUCTIONS", label: "Manage Auctions", description: "Create and manage product auctions" },
    { id: "MANAGE_LOTTERIES", label: "Manage Lotteries", description: "Create and manage product lotteries" }
];

export default function AppointUserModal({ type, onClose, onSubmit }) {
    const [username, setUsername] = useState("");
    const [selectedPermissions, setSelectedPermissions] = useState([]);
    const [error, setError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handlePermissionChange = (permissionId) => {
        setSelectedPermissions(prevPermissions => {
            if (prevPermissions.includes(permissionId)) {
                return prevPermissions.filter(id => id !== permissionId);
            } else {
                return [...prevPermissions, permissionId];
            }
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault(); // Prevent default form submission
        e.stopPropagation(); // Stop event bubbling

        if (!username.trim()) {
            setError("Username is required");
            return;
        }

        setIsSubmitting(true);
        setError("");

        try {
            await onSubmit(username, selectedPermissions);
            // Only close if submission was successful
            onClose();
        } catch (err) {
            setError(`Failed to appoint ${type}: ${err.message || "Unknown error"}`);
            setIsSubmitting(false);
        }
    };

    const handleClose = (e) => {
        e.preventDefault(); // Prevent any default behavior
        e.stopPropagation(); // Stop event bubbling

        if (isSubmitting) return; // Don't close while submitting

        onClose();
    };

    const handleCancel = (e) => {
        e.preventDefault(); // Prevent any default behavior
        e.stopPropagation(); // Stop event bubbling

        if (isSubmitting) return; // Don't close while submitting

        onClose();
    };

    return (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && handleClose(e)}>
            <div className="modal-container" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Appoint {type === "owner" ? "Store Owner" : "Store Manager"}</h2>
                    <button
                        className="close-modal-btn"
                        onClick={handleClose}
                        type="button"
                        disabled={isSubmitting}
                    >
                        ×
                    </button>
                </div>

                <div className="modal-body">
                    {error && <div className="error-text">{error}</div>}

                    <div className="form-group">
                        <label htmlFor="username">Username*</label>
                        <input
                            type="text"
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Enter username"
                            disabled={isSubmitting}
                        />
                    </div>

                    {type === "manager" && (
                        <div className="form-group">
                            <label>Permissions</label>
                            <div className="permissions-list">
                                {PERMISSION_OPTIONS.map(permission => (
                                    <div key={permission.id} className="permission-option">
                                        <input
                                            type="checkbox"
                                            id={`permission-${permission.id}`}
                                            checked={selectedPermissions.includes(permission.id)}
                                            onChange={() => handlePermissionChange(permission.id)}
                                            disabled={isSubmitting}
                                        />
                                        <label htmlFor={`permission-${permission.id}`}>
                                            {permission.label}
                                        </label>
                                    </div>
                                ))}
                            </div>
                            <p className="help-text">
                                {type === "manager" ?
                                    "Select specific permissions for this manager." :
                                    "Owners automatically have all permissions."}
                            </p>
                        </div>
                    )}
                </div>

                <div className="modal-footer">
                    <button
                        className="btn-cancel"
                        onClick={handleCancel}
                        type="button"
                        disabled={isSubmitting}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn-submit"
                        onClick={handleSubmit}
                        type="button"
                        disabled={isSubmitting || !username.trim()}
                    >
                        {isSubmitting ? "Processing..." : `Appoint ${type === "owner" ? "Owner" : "Manager"}`}
                    </button>
                </div>
            </div>
        </div>
    );
}