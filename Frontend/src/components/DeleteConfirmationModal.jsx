import React from "react";
import "../index.css";

export default function DeleteConfirmationModal({
                                                    itemType,
                                                    itemName,
                                                    onClose,
                                                    onConfirm,
                                                    isDeleting
                                                }) {
    return (
        <div className="modal-overlay">
            <div className="modal-container delete-confirmation">
                <div className="modal-header">
                    <h2>Confirm Deletion</h2>
                    <button className="close-modal-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    <div className="warning-icon">⚠️</div>
                    <p className="confirmation-message">
                        Are you sure you want to delete this {itemType}:
                        <strong> {itemName}</strong>?
                    </p>
                    <p className="warning-text">
                        This action cannot be undone.
                    </p>
                </div>

                <div className="modal-footer">
                    <button
                        className="btn-cancel"
                        onClick={onClose}
                        disabled={isDeleting}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn-delete"
                        onClick={onConfirm}
                        disabled={isDeleting}
                    >
                        {isDeleting ? "Deleting..." : `Delete ${itemType}`}
                    </button>
                </div>
            </div>
        </div>
    );
}