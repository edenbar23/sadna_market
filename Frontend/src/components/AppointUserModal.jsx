import React, { useState } from "react";
import "../index.css";

// TODO: need to match to our permissions
const PERMISSION_OPTIONS = [
    { id: "MANAGE_INVENTORY", label: "Manage Inventory" },
    { id: "MANAGE_ORDERS", label: "Manage Orders" },
    { id: "VIEW_STORE_HISTORY", label: "View Store History" },
    { id: "REPLY_TO_MESSAGES", label: "Reply to Messages" },
    { id: "VIEW_STORE_STATISTICS", label: "View Store Statistics" }
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

    const handleSubmit = async () => {
        if (!username.trim()) {
            setError("Username is required");
            return;
        }

        setIsSubmitting(true);

        try {
            await onSubmit(username, selectedPermissions);
        } catch (err) {
            setError(`Failed to appoint ${type}: ${err.message || "Unknown error"}`);
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-container">
                <div className="modal-header">
                    <h2>Appoint {type === "owner" ? "Store Owner" : "Store Manager"}</h2>
                    <button className="close-modal-btn" onClick={onClose}>×</button>
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
                        onClick={onClose}
                        disabled={isSubmitting}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn-submit"
                        onClick={handleSubmit}
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? "Processing..." : `Appoint ${type === "owner" ? "Owner" : "Manager"}`}
                    </button>
                </div>
            </div>
        </div>
    );
}